package com.naveen.payment_service.exception;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(String message) {
        super(message);
    }
}
