package com.naveen.repayment_service.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentOrderRequest(

        @NotNull
        Long loanId,

        @NotNull
        Long customerId,

        @NotNull
        Long repaymentId,

        @NotNull
        BigDecimal amount,

        @NotNull
        String currency,

        @NotNull
        String paymentType,

        @NotNull
        String idempotencyKey
) {
}