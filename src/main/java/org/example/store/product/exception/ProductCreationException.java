package org.example.store.product.exception;

public class ProductCreationException extends RuntimeException {
    public ProductCreationException(String identifier) {
        super("Failed to create product '" + identifier + "'.");
    }
}
