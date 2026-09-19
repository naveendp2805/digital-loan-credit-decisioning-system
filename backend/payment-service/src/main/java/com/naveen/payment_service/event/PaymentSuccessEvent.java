package com.naveen.payment_service.event;

import java.math.BigDecimal;

public record PaymentSuccessEvent(
        Long paymentId,
        Long loanId,
        Long customerId,
        Long repaymentId,
        BigDecimal amount,
        String currency,
        String transactionReference
) {
}