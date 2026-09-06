package com.naveen.repayment_service.repository;

import com.naveen.repayment_service.entity.Repayment;
import com.naveen.repayment_service.entity.RepaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    List<Repayment> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);

    Optional<Repayment> findByLoanIdAndInstallmentNumber(Long loanId, Integer installmentNumber);

    List<Repayment> findByLoanIdAndStatus(Long loanId, RepaymentStatus status);

    List<Repayment> findByStatusAndDueDateBefore(RepaymentStatus status, LocalDate date);

    boolean existsByLoanIdAndInstallmentNumber(Long loanId, Integer installmentNumber);
}
