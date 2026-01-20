package org.example.store.order.exception;

public class OrderPlacementException extends RuntimeException {
    public OrderPlacementException(String identifier) {
        super("Failed to place order '" + identifier + "'.");
    }
}
