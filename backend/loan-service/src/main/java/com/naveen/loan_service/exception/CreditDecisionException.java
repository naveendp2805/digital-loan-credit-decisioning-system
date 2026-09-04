package com.naveen.loan_service.exception;

public class CreditDecisionException extends RuntimeException {
    public CreditDecisionException(String message) {
        super(message);
    }
}
