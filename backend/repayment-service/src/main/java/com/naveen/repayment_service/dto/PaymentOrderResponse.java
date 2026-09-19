package com.naveen.repayment_service.dto;

import java.math.BigDecimal;

public record PaymentOrderResponse(
        Long paymentId,
        String razorpayOrderId,
        BigDecimal amount,
        String currency,
        String razorpayKeyId
) {
}