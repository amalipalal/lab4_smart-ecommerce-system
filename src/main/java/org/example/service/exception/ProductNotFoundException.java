package org.example.service.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String identifier) {
        super("Product '" + identifier + "' was not found.");
    }
}
