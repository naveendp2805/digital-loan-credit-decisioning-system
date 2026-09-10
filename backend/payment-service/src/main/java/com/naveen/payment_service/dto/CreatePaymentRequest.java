package com.naveen.payment_service.dto;

import com.naveen.payment_service.entity.PaymentType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreatePaymentRequest(

        @NotNull(message = "Loan ID is required")
        @Positive(message = "Loan ID must be positive")
        Long loanId,

        @NotNull(message = "Customer ID is required")
        @Positive(message = "Customer ID must be positive")
        Long customerId,

        Long repaymentId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
        @Digits(integer = 13, fraction = 2, message = "Amount must have maximum 2 decimal places")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter uppercase ISO code")
        String currency,

        @NotNull(message = "Payment type is required")
        PaymentType paymentType,

        @NotBlank(message = "Idempotency key is required")
        @Size(min = 10, max = 100, message = "Idempotency key must contain 10-100 characters")
        String idempotencyKey

) {
}
