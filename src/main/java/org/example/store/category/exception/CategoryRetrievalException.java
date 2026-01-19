package org.example.store.category.exception;

public class CategoryRetrievalException extends RuntimeException {
    public CategoryRetrievalException(String identifier) {
        super("Failed to retrieve '" + identifier + "'.");
    }
}
