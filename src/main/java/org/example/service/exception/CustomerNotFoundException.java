package org.example.service.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String identifier) {
        super("Customer with identity '" + identifier + "' was not found.");
    }
}
