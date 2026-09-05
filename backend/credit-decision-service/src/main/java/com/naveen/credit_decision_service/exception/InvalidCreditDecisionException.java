package com.naveen.credit_decision_service.exception;

public class InvalidCreditDecisionException extends RuntimeException {
    public InvalidCreditDecisionException(String message) {
        super(message);
    }
}
