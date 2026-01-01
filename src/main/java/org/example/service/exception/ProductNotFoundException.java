package org.example.service.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String identifier) {
        super("Product not found: " + identifier);
    }
}
