package com.naveen.credit_decision_service.dto;

import com.naveen.credit_decision_service.entity.CreditDecision;
import org.springframework.stereotype.Component;

@Component
public class CreditDecisionMapper {

    public CreditDecision toEntity(CreditDecisionRequest request) {
        return CreditDecision.builder()
                .loanId(request.getLoanId())
                .creditScore(request.getCreditScore())
                .monthlyIncome(request.getMonthlyIncome())
                .existingDebt(request.getExistingDebt())
                .loanAmount(request.getLoanAmount())
                .build();
    }

    public CreditDecisionResponse toResponse(CreditDecision decision) {
        return new CreditDecisionResponse(
                decision.getId(),
                decision.getLoanId(),
                decision.getCreditScore(),
                decision.getDecisionStatus(),
                decision.getDecisionReason(),
                decision.getMonthlyIncome(),
                decision.getExistingDebt(),
                decision.getLoanAmount(),
                decision.getDebtToIncomeRatio(),
                decision.getCreatedAt(),
                decision.getUpdatedAt());
    }
}
