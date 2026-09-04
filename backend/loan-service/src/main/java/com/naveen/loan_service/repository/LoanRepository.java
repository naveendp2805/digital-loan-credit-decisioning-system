package com.naveen.loan_service.repository;

import com.naveen.loan_service.entity.Loan;
import com.naveen.loan_service.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByCustomerId(Long customerId);

    List<Loan> findByLoanStatus(LoanStatus loanStatus);
}
