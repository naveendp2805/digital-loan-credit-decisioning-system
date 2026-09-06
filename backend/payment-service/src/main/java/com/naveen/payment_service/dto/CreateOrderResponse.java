package com.naveen.payment_service.dto;

import java.math.BigDecimal;

public record CreateOrderResponse(
        Long paymentId,

        Long loanId,

        BigDecimal amount,

        String currency,

        String paymentType,

        String status,

        String razorpayOrderId,

        String transactionReference,

        String razorpayKeyId
) {
}
