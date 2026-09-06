package com.naveen.repayment_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RepaymentRequest(

        @NotNull(message = "Loan ID is required")
        @Positive(message = "Loan ID must be positive")
        Long loanId,

        @NotNull(message = "Installment number is required")
        @Positive(message = "Installment number must be positive")
        Integer installmentNumber,

        @NotNull(message = "Payment amount is required")
        @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
        @Digits(integer = 13, fraction = 2, message = "Payment amount must have maximum 2 decimal places")
        BigDecimal paymentAmount
) {
}
