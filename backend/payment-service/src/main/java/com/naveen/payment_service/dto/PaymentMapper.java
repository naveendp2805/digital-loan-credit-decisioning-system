package com.naveen.payment_service.dto;

import com.naveen.payment_service.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getLoanId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentType().name(),
                payment.getStatus().name(),
                payment.getRazorpayOrderId(),
                payment.getRazorpayPaymentId(),
                payment.getTransactionReference(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    public CreateOrderResponse buildCreateOrderResponse(Payment payment, String razorpayKeyId) {
        return new CreateOrderResponse(
                payment.getId(),
                payment.getLoanId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentType().name(),
                payment.getStatus().name(),
                payment.getRazorpayOrderId(),
                payment.getTransactionReference(),
                razorpayKeyId
        );
    }
}
