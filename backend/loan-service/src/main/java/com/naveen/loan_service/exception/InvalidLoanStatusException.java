package com.naveen.loan_service.exception;

public class InvalidLoanStatusException extends RuntimeException {
    public InvalidLoanStatusException(String message) {
        super(message);
    }
}
