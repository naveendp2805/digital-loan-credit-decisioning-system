package com.naveen.credit_decision_service.repository;

import com.naveen.credit_decision_service.entity.CreditDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditDecisionRepository extends JpaRepository<CreditDecision, Long> {

    Optional<CreditDecision> findByLoanId(Long loanId);

    boolean existsByLoanId(Long loanId);
}
