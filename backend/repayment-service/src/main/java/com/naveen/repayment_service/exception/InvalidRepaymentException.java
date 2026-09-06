package com.naveen.repayment_service.exception;

public class InvalidRepaymentException extends RuntimeException {
    public InvalidRepaymentException(String message) {
        super(message);
    }
}
