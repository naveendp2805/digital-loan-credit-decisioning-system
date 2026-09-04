package com.naveen.loan_service.exception;

public class CustomerServiceException extends RuntimeException {
    public CustomerServiceException(String message) {
        super(message);
    }
}
