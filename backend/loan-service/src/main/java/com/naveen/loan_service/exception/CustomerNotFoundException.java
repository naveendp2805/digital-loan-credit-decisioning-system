package com.naveen.loan_service.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
            super("Customer not found with id: " + id);
    }
}
