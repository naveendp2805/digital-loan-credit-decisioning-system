package com.naveen.repayment_service.dto;

import com.naveen.repayment_service.entity.Repayment;
import org.springframework.stereotype.Component;

@Component
public class RepaymentMapper {

    public RepaymentResponse toResponse(Repayment repayment) {
        return new RepaymentResponse(
                repayment.getId(),
                repayment.getLoanId(),
                repayment.getCustomerId(),
                repayment.getInstallmentNumber(),
                repayment.getDueDate(),
                repayment.getInstallmentAmount(),
                repayment.getPaidAmount(),
                repayment.getRemainingAmount(),
                repayment.getStatus(),
                repayment.getPaidDate()
        );
    }
}
