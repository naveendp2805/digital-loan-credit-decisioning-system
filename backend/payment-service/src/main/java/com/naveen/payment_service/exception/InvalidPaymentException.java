package com.naveen.payment_service.exception;

public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
