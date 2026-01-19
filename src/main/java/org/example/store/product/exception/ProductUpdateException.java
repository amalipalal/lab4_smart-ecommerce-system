package org.example.store.product.exception;

public class ProductUpdateException extends RuntimeException {
    public ProductUpdateException(String identifier) {
        super("Failed to update the product '" + identifier + "'.");
    }
}
