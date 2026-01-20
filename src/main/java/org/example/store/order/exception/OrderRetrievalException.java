package org.example.store.order.exception;

public class OrderRetrievalException extends RuntimeException {
    public OrderRetrievalException(String identifier) {
        super("Failed to retrieve '" + identifier + "'.");
    }
}
