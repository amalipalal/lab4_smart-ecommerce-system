package org.example.store.category.exception;

public class CategoryUpdateException extends RuntimeException {
    public CategoryUpdateException(String identifier) {
        super("Failed to update the category '" + identifier + "'.");
    }
}
