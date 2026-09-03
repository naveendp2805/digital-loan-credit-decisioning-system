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

    private Long customerId;

    private BigDecimal loanAmount;

    private Integer tenureMonths;

    private BigDecimal monthlyIncome;
}
