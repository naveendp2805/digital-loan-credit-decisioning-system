package com.naveen.loan_service.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditDecisionRequest {

    private Long loanId;

    private Integer creditScore;

    private BigDecimal monthlyIncome;

    private BigDecimal existingDebt;

    private BigDecimal loanAmount;
}
