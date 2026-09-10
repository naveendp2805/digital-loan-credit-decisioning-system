package com.naveen.payment_service.service;

import com.naveen.payment_service.dto.CreateOrderResponse;
import com.naveen.payment_service.dto.CreatePaymentRequest;
import com.naveen.payment_service.dto.PaymentResponse;
import com.naveen.payment_service.entity.Payment;
import com.naveen.payment_service.entity.PaymentStatus;
import com.naveen.payment_service.entity.PaymentType;
import com.naveen.payment_service.exception.InvalidPaymentException;
import com.naveen.payment_service.exception.PaymentAlreadyCompletedException;
import com.naveen.payment_service.exception.PaymentAmountMismatchException;
import com.naveen.payment_service.exception.PaymentNotFoundException;
import com.naveen.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;
    private final PaymentMapper mapper;
    private final ExternalValidationService externalValidationService;
    private final WebhookEventService webhookEventService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;


    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    @Transactional
    public CreateOrderResponse createPayment(
            CreatePaymentRequest request
    ) throws RazorpayException {

        // 1. Validate customer
        externalValidationService.validateCustomer(
                request.customerId()
        );

        // 2. Validate loan and ownership
        externalValidationService.validateLoanOwnership(
                request.customerId(),
                request.loanId()
        );

        // 3. Idempotency check
        var existingPayment =
                paymentRepository.findByIdempotencyKey(
                        request.idempotencyKey()
                );

        if (existingPayment.isPresent()) {

            Payment existing = existingPayment.get();

            if (!existing.getLoanId()
                    .equals(request.loanId())
                    ||
                    !existing.getCustomerId()
                            .equals(request.customerId())) {

                throw new InvalidPaymentException(
                        "Idempotency key is already associated " +
                                "with a different payment"
                );
            }

            if (existing.getRepaymentId() != null
                    && request.repaymentId() != null
                    && !existing.getRepaymentId()
                    .equals(request.repaymentId())) {

                throw new InvalidPaymentException(
                        "Idempotency key is already associated " +
                                "with a different repayment"
                );
            }

            return mapper.buildCreateOrderResponse(
                    existing,
                    razorpayKeyId
            );
        }

        // 4. Validate payment type
        validatePaymentType(request);

        // 5. Repayment must have repaymentId
        if (request.paymentType()
                == PaymentType.REPAYMENT
                && request.repaymentId() == null) {

            throw new InvalidPaymentException(
                    "Repayment ID is required for repayment payments"
            );
        }

        // 6. Prevent duplicate successful repayment
        if (request.repaymentId() != null) {

            boolean alreadyPaid =
                    paymentRepository
                            .existsByRepaymentIdAndStatus(
                                    request.repaymentId(),
                                    PaymentStatus.SUCCESS
                            );

            if (alreadyPaid) {

                throw new PaymentAlreadyCompletedException(
                        "Repayment " +
                                request.repaymentId() +
                                " has already been paid"
                );
            }
        }

        // 7. Validate amount
        validateAmount(request.amount());

        // 8. Create local payment
        Payment payment = Payment.builder()
                .loanId(request.loanId())
                .customerId(request.customerId())
                .repaymentId(request.repaymentId())
                .amount(request.amount())
                .currency(
                        request.currency().toUpperCase()
                )
                .paymentType(request.paymentType())
                .status(PaymentStatus.CREATED)
                .idempotencyKey(
                        request.idempotencyKey()
                )
                .build();

        payment = paymentRepository.save(payment);

        // 9. Create Razorpay order
        String receipt =
                "PAY-" + payment.getId();

        Order order =
                razorpayService.createOrder(
                        payment.getAmount(),
                        payment.getCurrency(),
                        receipt
                );

        // 10. Store Razorpay order ID
        payment.setRazorpayOrderId(
                order.get("id")
        );

        payment.setStatus(
                PaymentStatus.PENDING
        );

        paymentRepository.save(payment);

        // 11. Return order information
        return mapper.buildCreateOrderResponse(
                payment,
                razorpayKeyId
        );
    }


    // =========================================================
    // VERIFY PAYMENT
    // =========================================================

    @Transactional
    public Payment verifyPayment(
            String orderId,
            String paymentId,
            String signature
    ) throws RazorpayException {

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(orderId)
                        .orElseThrow(() ->
                                new InvalidPaymentException(
                                        "Payment not found for Razorpay order: "
                                                + orderId
                                )
                        );

        // Already successfully processed
        if (payment.getStatus()
                == PaymentStatus.SUCCESS) {

            return payment;
        }

        // Prevent Razorpay payment ID reuse
        var existingPayment =
                paymentRepository
                        .findByRazorpayPaymentId(paymentId);

        if (existingPayment.isPresent()
                && !existingPayment.get()
                .getId()
                .equals(payment.getId())) {

            throw new InvalidPaymentException(
                    "Razorpay payment ID is already associated " +
                            "with another payment"
            );
        }

        // Verify Razorpay signature
        boolean valid =
                razorpayService.verifyPaymentSignature(
                        orderId,
                        paymentId,
                        signature
                );

        if (!valid) {

            throw new InvalidPaymentException(
                    "Invalid Razorpay payment signature"
            );
        }

        // Fetch actual payment from Razorpay
        Payment razorpayPayment =
                razorpayService.fetchPayment(
                        paymentId
                );

        // Verify order ID
        String razorpayOrderId =
                razorpayPayment.get("order_id");

        if (!orderId.equals(razorpayOrderId)) {

            throw new InvalidPaymentException(
                    "Razorpay payment does not belong " +
                            "to the expected order"
            );
        }

        // Verify amount
        long razorpayAmount =
                ((Number) razorpayPayment
                        .get("amount"))
                        .longValue();

        long expectedAmount =
                payment.getAmount()
                        .movePointRight(2)
                        .longValueExact();

        if (razorpayAmount != expectedAmount) {

            throw new PaymentAmountMismatchException(
                    "Razorpay payment amount does not match " +
                            "expected payment amount"
            );
        }

        // Verify actual Razorpay status
        String status =
                razorpayPayment.get("status");

        if (!"captured".equalsIgnoreCase(status)) {

            if ("failed".equalsIgnoreCase(status)) {

                payment.setStatus(
                        PaymentStatus.FAILED
                );

                payment.setFailureReason(
                        "Razorpay payment status: "
                                + status
                );

                return paymentRepository.save(payment);
            }

            throw new InvalidPaymentException(
                    "Razorpay payment is not captured. " +
                            "Current status: " + status
            );
        }

        // Mark SUCCESS
        payment.setRazorpayPaymentId(
                paymentId
        );

        payment.setRazorpaySignature(
                signature
        );

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        return paymentRepository.save(payment);
    }


    // =========================================================
    // GET PAYMENT
    // =========================================================

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(
            Long id
    ) {

        Payment payment =
                paymentRepository.findById(id)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        "Payment not found for ID: "
                                                + id
                                )
                        );

        return mapper.toResponse(payment);
    }


    // =========================================================
    // GET PAYMENTS BY LOAN
    // =========================================================

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByLoan(
            Long loanId
    ) {

        return paymentRepository
                .findByLoanId(loanId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }


    // =========================================================
    // GET PAYMENTS BY CUSTOMER
    // =========================================================

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByCustomer(
            Long customerId
    ) {

        return paymentRepository
                .findByCustomerId(customerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }


    // =========================================================
    // RAZORPAY WEBHOOK
    // =========================================================

    @Transactional
    public void processWebhook(
            String payload,
            String signature,
            String eventId
    ) {

        // 1. Validate event ID
        if (eventId == null || eventId.isBlank()) {

            throw new InvalidPaymentException(
                    "Missing Razorpay webhook event ID"
            );
        }

        // 2. Verify webhook signature
        boolean verified =
                razorpayService.verifyWebhookSignature(
                        payload,
                        signature
                );

        if (!verified) {

            throw new InvalidPaymentException(
                    "Invalid Razorpay webhook signature"
            );
        }

        // 3. Ignore duplicate webhook
        if (webhookEventService
                .alreadyProcessed(eventId)) {

            return;
        }

        // 4. Parse raw payload
        JSONObject webhook =
                new JSONObject(payload);

        String event =
                webhook.getString("event");

        // 5. Register webhook event
        webhookEventService.registerEvent(
                eventId,
                event
        );

        // 6. Process event
        switch (event) {

            case "payment.captured" ->
                    handlePaymentCaptured(webhook);

            case "payment.failed" ->
                    handlePaymentFailed(webhook);

            case "order.paid" ->
                    handleOrderPaid(webhook);

            default ->
                    handleUnknownEvent(event);
        }

        // 7. Mark webhook processed
        webhookEventService.markProcessed(
                eventId
        );
    }


    // =========================================================
    // PAYMENT CAPTURED
    // =========================================================

    private void handlePaymentCaptured(
            JSONObject webhook
    ) {

        JSONObject paymentEntity =
                webhook
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

        String razorpayPaymentId =
                paymentEntity.getString("id");

        String razorpayOrderId =
                paymentEntity.optString("order_id");

        if (razorpayOrderId == null
                || razorpayOrderId.isBlank()) {

            return;
        }

        paymentRepository
                .findByRazorpayOrderId(
                        razorpayOrderId
                )
                .ifPresent(payment -> {

                    // Already SUCCESS
                    if (payment.getStatus()
                            == PaymentStatus.SUCCESS) {

                        return;
                    }

                    // Verify webhook amount
                    long webhookAmount =
                            paymentEntity
                                    .getLong("amount");

                    long expectedAmount =
                            payment.getAmount()
                                    .movePointRight(2)
                                    .longValueExact();

                    if (webhookAmount
                            != expectedAmount) {

                        payment.setStatus(
                                PaymentStatus.FAILED
                        );

                        payment.setFailureReason(
                                "Webhook payment amount mismatch"
                        );

                        paymentRepository.save(payment);

                        return;
                    }

                    payment.setRazorpayPaymentId(
                            razorpayPaymentId
                    );

                    payment.setStatus(
                            PaymentStatus.SUCCESS
                    );

                    paymentRepository.save(payment);
                });
    }


    // =========================================================
    // PAYMENT FAILED
    // =========================================================

    private void handlePaymentFailed(
            JSONObject webhook
    ) {

        JSONObject paymentEntity =
                webhook
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

        String razorpayPaymentId =
                paymentEntity.getString("id");

        String razorpayOrderId =
                paymentEntity.optString("order_id");

        if (razorpayOrderId == null
                || razorpayOrderId.isBlank()) {

            return;
        }

        paymentRepository
                .findByRazorpayOrderId(
                        razorpayOrderId
                )
                .ifPresent(payment -> {

                    // Never move SUCCESS back to FAILED
                    if (payment.getStatus()
                            == PaymentStatus.SUCCESS) {

                        return;
                    }

                    payment.setRazorpayPaymentId(
                            razorpayPaymentId
                    );

                    String failureReason =
                            paymentEntity.optString(
                                    "error_description",
                                    "Razorpay payment failed"
                            );

                    payment.setFailureReason(
                            failureReason
                    );

                    payment.setStatus(
                            PaymentStatus.FAILED
                    );

                    paymentRepository.save(payment);
                });
    }


    // =========================================================
    // ORDER PAID
    // =========================================================

    private void handleOrderPaid(
            JSONObject webhook
    ) {

        JSONObject orderEntity =
                webhook
                        .getJSONObject("payload")
                        .getJSONObject("order")
                        .getJSONObject("entity");

        String razorpayOrderId =
                orderEntity.getString("id");

        paymentRepository
                .findByRazorpayOrderId(
                        razorpayOrderId
                )
                .ifPresent(payment -> {

                    // Already SUCCESS
                    if (payment.getStatus()
                            == PaymentStatus.SUCCESS) {

                        return;
                    }

                    /*
                     * Do not blindly mark SUCCESS based only
                     * on order.paid.
                     *
                     * Fetch the actual Razorpay order/payment
                     * in the next integration if needed.
                     */
                    payment.setStatus(
                            PaymentStatus.SUCCESS
                    );

                    paymentRepository.save(payment);
                });
    }


    // =========================================================
    // UNKNOWN WEBHOOK EVENT
    // =========================================================

    private void handleUnknownEvent(
            String event
    ) {

        /*
         * Unknown Razorpay events should not break
         * webhook processing.
         *
         * Add structured logging here later.
         */
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validatePaymentType(
            CreatePaymentRequest request
    ) {

        if (request.paymentType() == null) {

            throw new InvalidPaymentException(
                    "Payment type is required"
            );
        }

        /*
         * Your PaymentType enum currently doesn't contain
         * DISBURSEMENT, so this check is mostly defensive.
         */
        if ("DISBURSEMENT".equals(
                request.paymentType().name()
        )) {

            throw new InvalidPaymentException(
                    "DISBURSEMENT is not supported by " +
                            "Razorpay collection flow"
            );
        }
    }


    private void validateAmount(
            BigDecimal amount
    ) {

        if (amount == null
                || amount.signum() <= 0) {

            throw new InvalidPaymentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (amount.scale() > 2) {

            throw new InvalidPaymentException(
                    "Payment amount can contain at most " +
                            "2 decimal places"
            );
        }
    }
}