package com.naveen.credit_decision_service.dto;

import com.naveen.credit_decision_service.entity.DecisionReason;
import com.naveen.credit_decision_service.entity.DecisionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditDecisionResponse {

    private Long id;

    private Long loanId;

    private Integer creditScore;

    private DecisionStatus decisionStatus;

    private DecisionReason decisionReason;

    private BigDecimal monthlyIncome;

    private BigDecimal existingDebt;

    private BigDecimal loanAmount;

    private BigDecimal debtToIncomeRatio;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
