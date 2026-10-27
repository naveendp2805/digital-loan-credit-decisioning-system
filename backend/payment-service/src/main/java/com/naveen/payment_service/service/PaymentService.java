package com.naveen.payment_service.service;

import com.naveen.payment_service.client.CustomerClient;
import com.naveen.payment_service.client.LoanClient;
import com.naveen.payment_service.client.RepaymentClient;
import com.naveen.payment_service.dto.*;
import com.naveen.payment_service.entity.LoanResponse;
import com.naveen.payment_service.entity.Payment;
import com.naveen.payment_service.entity.PaymentStatus;
import com.naveen.payment_service.entity.PaymentType;
import com.naveen.payment_service.exception.InvalidPaymentException;
import com.naveen.payment_service.exception.PaymentAlreadyCompletedException;
import com.naveen.payment_service.exception.PaymentAmountMismatchException;
import com.naveen.payment_service.exception.PaymentNotFoundException;
import com.naveen.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;
    private final PaymentMapper mapper;
    private final ExternalValidationService externalValidationService;
    private final PaymentEventPublisher paymentEventPublisher;
    private final CustomerClient customerClient;
    private final LoanClient loanClient;
    private final RepaymentClient repaymentClient;

    @Value("${razorpay.key}")
    private String razorpayKeyId;

    @Transactional
    public CreateOrderResponse createPayment(CreatePaymentRequest request, Authentication authentication) throws RazorpayException {
        authorizeLoanAccess(request.loanId(), authentication);

        externalValidationService.validateCustomer(request.customerId());
        externalValidationService.validateLoanOwnership(request.customerId(), request.loanId());

        var existingPayment =
                paymentRepository.findByIdempotencyKey(
                        request.idempotencyKey()
                );

        if (existingPayment.isPresent()) {

            Payment existing = existingPayment.get();

            if (!existing.getLoanId().equals(request.loanId())
                    || !existing.getCustomerId().equals(request.customerId())) {

                throw new InvalidPaymentException(
                        "Idempotency key is already associated with a different payment"
                );
            }

            if (existing.getRepaymentId() != null
                    && request.repaymentId() != null
                    && !existing.getRepaymentId().equals(request.repaymentId())) {

                throw new InvalidPaymentException(
                        "Idempotency key is already associated with a different repayment"
                );
            }

            return mapper.buildCreateOrderResponse(
                    existing,
                    razorpayKeyId
            );
        }

        validatePaymentType(request);

        if (request.paymentType() == PaymentType.REPAYMENT
                && request.repaymentId() == null) {

            throw new InvalidPaymentException(
                    "Repayment ID is required for repayment payments"
            );
        }


        if (request.paymentType() == PaymentType.REPAYMENT) {

            if (request.repaymentId() == null) {
                throw new InvalidPaymentException(
                        "Repayment ID is required for repayment payments"
                );
            }

            final RepaymentResponse repayment;

            try {
                repayment = repaymentClient.getRepaymentById(
                        request.repaymentId()
                );
            } catch (FeignException.NotFound ex) {
                throw new InvalidPaymentException(
                        "Repayment not found: " + request.repaymentId()
                );
            } catch (FeignException ex) {
                throw new InvalidPaymentException(
                        "Unable to validate repayment because Repayment Service is unavailable"
                );
            }

            if (!request.loanId().equals(repayment.loanId())
                    || !request.customerId().equals(repayment.customerId())) {
                throw new InvalidPaymentException(
                        "Repayment does not belong to the specified loan and customer"
                );
            }

            if ("PAID".equalsIgnoreCase(repayment.status())) {
                throw new PaymentAlreadyCompletedException(
                        "Repayment " + request.repaymentId()
                                + " has already been paid"
                );
            }

            BigDecimal remainingAmount = repayment.remainingAmount();

            if (remainingAmount == null
                    || remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new PaymentAlreadyCompletedException(
                        "Repayment has no outstanding balance"
                );
            }

            if (request.amount().compareTo(remainingAmount) > 0) {
                throw new InvalidPaymentException(
                        "Payment amount exceeds repayment remaining amount"
                );
            }

            List<Payment> existingPayments =
                    paymentRepository.findByRepaymentId(
                            request.repaymentId()
                    );

            boolean outstandingOrderExists = existingPayments.stream()
                    .anyMatch(payment ->
                            payment.getStatus() == PaymentStatus.CREATED
                                    || payment.getStatus() == PaymentStatus.PENDING
                    );

            if (outstandingOrderExists) {
                throw new InvalidPaymentException(
                        "An existing payment order is still pending for this repayment"
                );
            }
        }


        validateAmount(request.amount());

        Payment payment = Payment.builder()
                .loanId(request.loanId())
                .customerId(request.customerId())
                .repaymentId(request.repaymentId())
                .amount(request.amount())
                .currency(request.currency().toUpperCase())
                .paymentType(request.paymentType())
                .status(PaymentStatus.CREATED)
                .idempotencyKey(request.idempotencyKey())
                .transactionReference(
                        "TXN-" + UUID.randomUUID()
                )
                .build();

        payment = paymentRepository.save(payment);

        String receipt = "PAY-" + payment.getId();

        Order order =
                razorpayService.createOrder(
                        payment.getAmount(),
                        payment.getCurrency(),
                        receipt
                );

        payment.setRazorpayOrderId(
                order.get("id")
        );

        payment.setStatus(
                PaymentStatus.PENDING
        );

        paymentRepository.save(payment);

        return mapper.buildCreateOrderResponse(
                payment,
                razorpayKeyId
        );
    }

    @Transactional
    public Payment verifyPayment(
            String orderId,
            String paymentId,
            String signature
    ) throws RazorpayException {

        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                        .orElseThrow(() -> new InvalidPaymentException("Payment not found for Razorpay order: " + orderId));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return payment;
        }

        var existingPayment =
                paymentRepository.findByRazorpayPaymentId(
                        paymentId
                );

        if (existingPayment.isPresent()
                && !existingPayment.get().getId().equals(payment.getId())) {

            throw new InvalidPaymentException(
                    "Razorpay payment ID is already associated with another payment"
            );
        }

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

        com.razorpay.Payment razorpayPayment =
                razorpayService.fetchPayment(paymentId);

        String razorpayOrderId =
                razorpayPayment.get("order_id");

        if (!orderId.equals(razorpayOrderId)) {

            throw new InvalidPaymentException(
                    "Razorpay payment does not belong to the expected order"
            );
        }

        long razorpayAmount =
                ((Number) razorpayPayment.get("amount"))
                        .longValue();

        long expectedAmount =
                payment.getAmount()
                        .movePointRight(2)
                        .longValueExact();

        if (razorpayAmount != expectedAmount) {

            throw new PaymentAmountMismatchException(
                    "Razorpay payment amount does not match expected payment amount"
            );
        }

        String status =
                razorpayPayment.get("status");

        if (!"captured".equalsIgnoreCase(status)) {

            if ("failed".equalsIgnoreCase(status)) {

                payment.setStatus(
                        PaymentStatus.FAILED
                );

                payment.setFailureReason(
                        "Razorpay payment status: " + status
                );

                return paymentRepository.save(payment);
            }

            throw new InvalidPaymentException(
                    "Razorpay payment is not captured. Current status: "
                            + status
            );
        }

        payment.setRazorpayPaymentId(
                paymentId
        );

        payment.setRazorpaySignature(
                signature
        );

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        Payment savedPayment =
                paymentRepository.save(payment);

        paymentEventPublisher.publishPaymentSuccess(
                savedPayment
        );

        return savedPayment;
    }

    @Transactional
    public Payment reconcileCapturedPayment(
            Long localPaymentId,
            String razorpayPaymentId
    ) throws RazorpayException {

        Payment payment = paymentRepository.findById(localPaymentId)
                .orElseThrow(() ->
                        new InvalidPaymentException(
                                "Local payment not found: " + localPaymentId
                        )
                );

        // Idempotency: don't process an already successful payment again.
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (!razorpayPaymentId.equals(payment.getRazorpayPaymentId())) {
                throw new InvalidPaymentException(
                        "Payment is already successful with a different Razorpay payment ID"
                );
            }
            return payment;
        }

        // Prevent a Razorpay payment ID from being reused locally.
        var existingPayment =
                paymentRepository.findByRazorpayPaymentId(razorpayPaymentId);

        if (existingPayment.isPresent()
                && !existingPayment.get().getId().equals(payment.getId())) {
            throw new InvalidPaymentException(
                    "Razorpay payment ID is already associated with another payment"
            );
        }

        // Retrieve authoritative payment details from Razorpay.
        com.razorpay.Payment razorpayPayment =
                razorpayService.fetchPayment(razorpayPaymentId);

        String razorpayOrderId = razorpayPayment.get("order_id");

        if (!payment.getRazorpayOrderId().equals(razorpayOrderId)) {
            throw new InvalidPaymentException(
                    "Razorpay payment does not belong to the expected order"
            );
        }

        long actualAmount =
                ((Number) razorpayPayment.get("amount")).longValue();

        long expectedAmount = payment.getAmount()
                .movePointRight(2)
                .longValueExact();

        if (actualAmount != expectedAmount) {
            throw new PaymentAmountMismatchException(
                    "Razorpay payment amount does not match expected payment amount"
            );
        }

        String status = razorpayPayment.get("status");

        if (!"captured".equalsIgnoreCase(status)) {
            throw new InvalidPaymentException(
                    "Razorpay payment is not captured. Current status: " + status
            );
        }

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        paymentEventPublisher.publishPaymentSuccess(savedPayment);

        return savedPayment;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id, Authentication authentication) {

        Payment payment = paymentRepository.findById(id)
                        .orElseThrow(() -> new PaymentNotFoundException("Payment not found for ID: " + id));

        authorizeLoanAccess(payment.getLoanId(), authentication);

        return mapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByLoan(Long loanId, Authentication authentication) {
        authorizeLoanAccess(loanId, authentication);


        return paymentRepository
                .findByLoanId(loanId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

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

    private void validatePaymentType(
            CreatePaymentRequest request
    ) {

        if (request.paymentType() == null) {

            throw new InvalidPaymentException(
                    "Payment type is required"
            );
        }

        if ("DISBURSEMENT".equals(
                request.paymentType().name()
        )) {

            throw new InvalidPaymentException(
                    "DISBURSEMENT is not supported by Razorpay collection flow"
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
                    "Payment amount can contain at most 2 decimal places"
            );
        }
    }

    private void authorizeLoanAccess(Long loanId, Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (isAdmin) {
            return;
        }

        LoanResponse loan;

        try {
            loan = loanClient.getLoanById(loanId);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Loan not found with ID: " + loanId);
        } catch (FeignException e) {
            throw new RuntimeException("Loan service is unavailable");
        }

        CustomerResponse customer;

        try {
            customer = customerClient.getCustomerById(loan.customerId());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Customer not found with ID: " + loan.customerId());
        } catch (FeignException e) {
            throw new RuntimeException("Customer service is unavailable");
        }

        String authenticatedEmail = authentication.getName();

        if (!authenticatedEmail.equalsIgnoreCase(customer.getEmail())) {
            throw new AccessDeniedException("You are not authorized to access this loan");
        }
    }
}