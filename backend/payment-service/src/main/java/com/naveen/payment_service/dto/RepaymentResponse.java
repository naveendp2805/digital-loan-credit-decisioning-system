package com.naveen.payment_service.dto;

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
        String status,
        LocalDate paidDate
) {
}