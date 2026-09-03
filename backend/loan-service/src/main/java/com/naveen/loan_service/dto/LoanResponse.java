package com.naveen.loan_service.dto;

import com.naveen.loan_service.entity.LoanStatus;
import com.naveen.loan_service.entity.LoanType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class LoanResponse {

    private Long id;

    private Long customerId;

    private LoanType loanType;

    private BigDecimal loanAmount;

    private Integer tenureMonths;

    private BigDecimal interestRate;

    private BigDecimal monthlyIncome;

    private BigDecimal emi;

    private String purpose;

    private LoanStatus status;

    private Integer creditScore;

    private String rejectionReason;

    private LocalDateTime applicationDate;

    private LocalDateTime approvedDate;
}
