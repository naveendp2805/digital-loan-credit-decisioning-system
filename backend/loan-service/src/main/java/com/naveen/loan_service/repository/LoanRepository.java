package com.naveen.loan_service.repository;

import com.naveen.loan_service.entity.Loan;
import com.naveen.loan_service.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByCustomerId(Long customerId);

    List<Loan> findByLoanStatus(LoanStatus loanStatus);

    @Query("""
            SELECT COALESCE(SUM(l.emi), 0)
            FROM Loan l 
            WHERE l.customerId = :customerId
            AND l.loanStatus IN (
                com.naveen.loan_service.entity.LoanStatus.APPROVED,
                com.naveen.loan_service.entity.LoanStatus.PENDING
            ) 
    """)
    BigDecimal calculateExistingMonthlyDebt(@Param("customerId") Long customerId);
}
