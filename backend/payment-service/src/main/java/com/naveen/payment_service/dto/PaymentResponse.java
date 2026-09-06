package com.naveen.payment_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,

        Long loanId,

        Long customerId,

        BigDecimal amount,

        String currency,

        String paymentType,

        String status,

        String razorpayOrderId,

        String razorpayPaymentId,

        String transactionReference,

        String failureReason,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
