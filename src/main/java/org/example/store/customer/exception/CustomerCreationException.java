package org.example.store.customer.exception;

public class CustomerCreationException extends RuntimeException {
    public CustomerCreationException(String identifier) {
        super("Failed to create customer '" + identifier + "'.");
    }
}
