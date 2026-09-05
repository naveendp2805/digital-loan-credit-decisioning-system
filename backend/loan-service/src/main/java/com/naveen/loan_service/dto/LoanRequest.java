package com.naveen.loan_service.dto;

import com.naveen.loan_service.entity.LoanType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanRequest {

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;


    @NotNull(message = "Loan type is required")
    private LoanType loanType;


    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "10000.00", message = "Loan amount must be at least 10000")
    @DecimalMax(value = "5000000.00", message = "Loan amount cannot exceed 5000000")
    private BigDecimal loanAmount;


    @NotNull(message = "Tenure is required")
    @Min(value = 3, message = "Minimum tenure is 3 months")
    @Max(value = 360, message = "Maximum tenure is 360 months")
    private Integer tenureMonths;

    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 900, message = "Credit score cannot exceed 900")
    private Integer creditScore;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "10000.00", message = "Monthly income must be at least 10000")
    @DecimalMax(value = "100000000.00", message = "Monthly income is too high")
    private BigDecimal monthlyIncome;


    @Size(max = 255, message = "Purpose cannot exceed 255 characters")
    private String purpose;
}