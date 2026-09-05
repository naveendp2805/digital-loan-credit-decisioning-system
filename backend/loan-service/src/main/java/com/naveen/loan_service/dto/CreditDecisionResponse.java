package com.naveen.loan_service.dto;

import com.naveen.loan_service.entity.CreditDecision;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreditDecisionResponse {

    private Long id;

    private Long loanId;

    private Integer creditScore;

    private String decisionStatus;

    private String decisionReason;

    private BigDecimal monthlyIncome;

    private BigDecimal existingDebt;

    private BigDecimal loanAmount;

    private BigDecimal debtToIncomeRatio;
}
