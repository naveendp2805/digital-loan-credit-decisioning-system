package com.naveen.payment_service.service;

import com.naveen.payment_service.dto.*;
import com.naveen.payment_service.entity.Payment;
import com.naveen.payment_service.entity.PaymentStatus;
import com.naveen.payment_service.exception.DuplicatePaymentException;
import com.naveen.payment_service.exception.InvalidPaymentException;
import com.naveen.payment_service.exception.PaymentNotFoundException;
import com.naveen.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;
    private final PaymentMapper mapper;
    private final ExternalValidationService externalValidationService;

    @Value("${razorpay.key}")
    private String razorpayKeyId;

    @Transactional
    public CreateOrderResponse createPayment(CreatePaymentRequest request) {

        externalValidationService.validateCustomer(request.customerId());

        externalValidationService.validateLoanOwnership(request.customerId(), request.loanId());

        var existingPayment = paymentRepository.findByIdempotencyKey(request.idempotencyKey());

        if(existingPayment.isPresent())
            return mapper.buildCreateOrderResponse(existingPayment.get(), razorpayKeyId);

        switch (request.paymentType()) {
            case PROCESSING_FEE,
                 APPLICATION_FEE,
                 REPAYMENT,
                 DISBURSEMENT -> {}

            default -> throw new InvalidPaymentException("Unsupported payment type");
        }

        Payment payment = Payment.builder()
                .loanId(request.loanId())
                .customerId(request.customerId())
                .amount(request.amount())
                .currency(request.currency())
                .paymentType(request.paymentType())
                .status(PaymentStatus.CREATED)
                .idempotencyKey(request.idempotencyKey())
                .transactionReference(generateTransactionReference())
                .build();

        payment = paymentRepository.save(payment);

        try {
            Order razorpayOrder = razorpayService.createOrder(payment.getAmount(), payment.getCurrency(), payment.getTransactionReference());

            payment.setRazorpayOrderId(razorpayOrder.get("id"));
            payment.setStatus(PaymentStatus.PENDING);

            payment = paymentRepository.save(payment);

            return mapper.buildCreateOrderResponse(payment, razorpayKeyId);
        } catch(Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Unable to create Razorpay Order");

            paymentRepository.save(payment);

            throw new InvalidPaymentException("Unable to create Razorpay order");
        }
    }

    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {

        Payment payment = paymentRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for Razorpay order: " + request.razorpayOrderId()));

        if(payment.getStatus() == PaymentStatus.SUCCESS)
            return mapper.toResponse(payment);

        var existingPayment = paymentRepository.findByRazorpayPaymentId(request.razorpayPaymentId());

        if(existingPayment.isPresent() && !existingPayment.get().getId().equals(payment.getId()))
            throw new DuplicatePaymentException("Razorpay payment ID is already associated with another payment");

        boolean verified = razorpayService.verifyPaymentSignature(request.razorpayOrderId(), request.razorpayPaymentId(), request.razorpaySignature());

        if(!verified) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason( "Invalid Razorpay payment signature");

            paymentRepository.save(payment);

            throw new InvalidPaymentException("Payment verification failed");
        }

        payment.setRazorpayPaymentId(request.razorpayPaymentId());
        payment.setRazorpaySignature(request.razorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setFailureReason(null);

        paymentRepository.save(payment);

        return mapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for ID: " + id));

        return mapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByLoan(Long loanId) {
        return paymentRepository
                .findByLoanId(loanId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByCustomer(Long customerId) {
        return paymentRepository
                .findByCustomerId(customerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public void processWebhook(String payload, String signature) {
        boolean verified = razorpayService.verifyWebhookSignature(payload, signature);

        if(!verified)
            throw new InvalidPaymentException("Invalid Razorpay webhook signature");

        JSONObject webhook = new JSONObject(payload);

        String event = webhook.getString("event");

        if("payment.captured".equals(event))
        {
            JSONObject paymentEntity = webhook.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String razorpayPaymentId =
                    paymentEntity.getString("id");

            String razorpayOrderId =
                    paymentEntity.getString("order_id");

            paymentRepository
                    .findByRazorpayOrderId(razorpayOrderId)
                    .ifPresent(payment -> {

                        if (payment.getStatus() != PaymentStatus.SUCCESS) {

                            payment.setRazorpayPaymentId(razorpayPaymentId);
                            payment.setStatus(PaymentStatus.SUCCESS);

                            paymentRepository.save(payment);
                        }
                    });
        }
    }

    private String generateTransactionReference() {
        return "TXN-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();
    }


}
