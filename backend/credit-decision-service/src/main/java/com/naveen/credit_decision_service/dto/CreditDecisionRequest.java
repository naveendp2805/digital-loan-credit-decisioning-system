package com.naveen.credit_decision_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreditDecisionRequest {

    @NotNull(message = "Loan ID is required")
    @Positive(message = "Loan ID must be positive")
    private Long loanId;

    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 900, message = "Credit score cannot exceed 900")
    private Integer creditScore;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "1000.00", message = "Monthly income must be at least 1000")
    @Digits(integer = 13, fraction = 2, message = "Invalid monthly income")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Existing debt is required")
    @DecimalMin(value = "0.00", message = "Existing debt cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid existing debt")
    private BigDecimal existingDebt;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00", message = "Loan amount must be at least 1000")
    @Digits(integer = 13, fraction = 2, message = "Invalid loan amount")
    private BigDecimal loanAmount;
}
