package org.example.service.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String identifier) {
        super("Customer not found: " + identifier);
    }
}
