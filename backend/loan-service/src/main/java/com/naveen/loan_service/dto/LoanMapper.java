package com.naveen.loan_service.dto;

import com.naveen.loan_service.entity.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public Loan toEntity(LoanRequest request) {
        return Loan.builder()
                .customerId(request.getCustomerId())
                .loanType(request.getLoanType())
                .loanAmount(request.getLoanAmount())
                .tenureMonths(request.getTenureMonths())
                .monthlyIncome(request.getMonthlyIncome())
                .purpose(request.getPurpose())
                .build();
    }

    public LoanResponse toResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomerId())
                .loanType(loan.getLoanType())
                .loanAmount(loan.getLoanAmount())
                .tenureMonths(loan.getTenureMonths())
                .interestRate(loan.getInterestRate())
                .monthlyIncome(loan.getMonthlyIncome())
                .emi(loan.getEmi())
                .purpose(loan.getPurpose())
                .status(loan.getLoanStatus())
                .creditScore(loan.getCreditScore())
                .rejectionReason(loan.getRejectionReason())
                .applicationDate(loan.getApplicationDate())
                .approvedDate(loan.getApprovedDate())
                .build();
    }
}
