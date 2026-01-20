package org.example.store.customer.exception;

public class CustomerRetrievalException extends RuntimeException {
    public CustomerRetrievalException(String identifier) {
        super("Failed to retrieve '" + identifier + "'.");
    }
}
