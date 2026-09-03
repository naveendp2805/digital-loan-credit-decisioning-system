package com.naveen.loan_service.dto;

import com.naveen.loan_service.entity.CreditDecision;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreditDecisionResponse {

    private Long loanId;

    private Integer creditScore;

    private CreditDecision decision;

    private String reason;
}
