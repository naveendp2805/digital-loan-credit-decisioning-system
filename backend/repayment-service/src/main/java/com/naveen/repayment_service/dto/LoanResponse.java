package com.naveen.repayment_service.dto;

import java.math.BigDecimal;

public record LoanResponse(

        Long id,

        Long customerId,

        BigDecimal loanAmount,

        BigDecimal emi,

        BigDecimal interestRate,

        Integer tenureMonths,

        String status
) {
}
