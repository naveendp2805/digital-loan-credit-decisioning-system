package com.naveen.repayment_service.dto;

import com.naveen.repayment_service.entity.RepaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepaymentResponse(

        Long id,

        Long loanId,

        Long customerId,

        Integer installmentNumber,

        LocalDate dueDate,

        BigDecimal installmentAmount,

        BigDecimal paidAmount,

        BigDecimal remainingAmount,

        RepaymentStatus status,

        LocalDate paidDate
) {
}
