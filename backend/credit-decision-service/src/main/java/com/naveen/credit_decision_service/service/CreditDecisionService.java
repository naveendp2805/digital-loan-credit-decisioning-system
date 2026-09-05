package com.naveen.credit_decision_service.service;

import com.naveen.credit_decision_service.dto.CreditDecisionMapper;
import com.naveen.credit_decision_service.dto.CreditDecisionRequest;
import com.naveen.credit_decision_service.dto.CreditDecisionResponse;
import com.naveen.credit_decision_service.entity.CreditDecision;
import com.naveen.credit_decision_service.entity.DecisionReason;
import com.naveen.credit_decision_service.entity.DecisionStatus;
import com.naveen.credit_decision_service.exception.InvalidCreditDecisionException;
import com.naveen.credit_decision_service.exception.ResourceNotFoundException;
import com.naveen.credit_decision_service.repository.CreditDecisionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CreditDecisionService {

    private final CreditDecisionRepository creditDecisionRepository;

    private final CreditDecisionMapper mapper;

    public CreditDecisionResponse makeDecision(CreditDecisionRequest request) {

        if(creditDecisionRepository.existsByLoanId(request.getLoanId()))
            throw new InvalidCreditDecisionException("Credit Decision already exists for Loan ID: " + request.getLoanId());

        validateBusinessRules(request);

        BigDecimal dti = calculateDTI(request.getExistingDebt(), request.getMonthlyIncome());

        DecisionResult result = evaluateDecision(request.getCreditScore(), dti);

        CreditDecision decision = mapper.toEntity(request);

        decision.setDebtToIncomeRatio(dti);
        decision.setDecisionStatus(result.status());
        decision.setDecisionReason(result.reason());

        LocalDateTime now = LocalDateTime.now();

        decision.setCreatedAt(now);
        decision.setUpdatedAt(now);

        CreditDecision saved = creditDecisionRepository.save(decision);

        return mapper.toResponse(saved);
    }

    @Transactional
    public CreditDecisionResponse getDecisionById(long id) {
        CreditDecision decision = creditDecisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Decision not found with ID: " + id));

        return mapper.toResponse(decision);
    }

    @Transactional
    public CreditDecisionResponse getDecisionByLoanId(long loanId) {
        CreditDecision decision = creditDecisionRepository.findByLoanId(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Decision not found with Loan ID: " + loanId));

        return mapper.toResponse(decision);
    }

    @Transactional
    public List<CreditDecisionResponse> getAllDecisions() {
        return creditDecisionRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public String deleteDecision(Long id) {
        if(!creditDecisionRepository.existsById(id))
            throw new ResourceNotFoundException("Credit Decision not fount with ID: " + id);

        creditDecisionRepository.deleteById(id);

        return "Credit Decision with ID: " + id + " is deleted Successfully";
    }

    private void validateBusinessRules(CreditDecisionRequest request) {

        if(request.getExistingDebt().compareTo(request.getMonthlyIncome()) > 0)
            throw new InvalidCreditDecisionException("Existing debt cannot exceed monthly income");

        if(request.getLoanAmount().compareTo(request.getMonthlyIncome().multiply(BigDecimal.valueOf(100))) > 0)
            throw new InvalidCreditDecisionException("Loan amount is usually high compared to income");
    }

    private BigDecimal calculateDTI(BigDecimal existingDebt, BigDecimal monthlyIncome) {
        return existingDebt.divide(monthlyIncome, 4, RoundingMode.HALF_EVEN)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    private DecisionResult evaluateDecision(Integer creditScore, BigDecimal dti) {
        boolean strongCredit = creditScore >= 750;
        boolean moderateCredit = creditScore >= 650;

        boolean lowDti = dti.compareTo(BigDecimal.valueOf(30)) <= 0;
        boolean moderateDti = dti.compareTo(BigDecimal.valueOf(40)) <= 0;

        if(strongCredit && lowDti)
            return new DecisionResult(DecisionStatus.APPROVED, DecisionReason.HIGH_CREDIT_SCORE);

        if(!moderateCredit || !moderateDti)
        {
            if(!moderateCredit)
                return new DecisionResult(DecisionStatus.REJECTED, DecisionReason.LOW_CREDIT_SCORE);

            return new DecisionResult(DecisionStatus.REJECTED, DecisionReason.HIGH_DEBT_TO_INCOME_RATIO);
        }

        return new DecisionResult(DecisionStatus.REVIEW, DecisionReason.MODERATE_RISK);
    }

    private record DecisionResult(DecisionStatus status, DecisionReason reason) {}
}
