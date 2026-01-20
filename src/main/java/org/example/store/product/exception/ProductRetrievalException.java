package org.example.store.product.exception;

public class ProductRetrievalException extends RuntimeException {
    public ProductRetrievalException(String identifier) {
        super("Failed to retrieve '" + identifier + "'.");
    }
}
