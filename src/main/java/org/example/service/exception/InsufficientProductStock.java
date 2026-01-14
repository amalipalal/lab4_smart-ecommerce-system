package org.example.service.exception;

public class InsufficientProductStock extends RuntimeException {
    public InsufficientProductStock(String productId) {
        super("Insufficient stock of product '" + productId + "'.");
    }
}
