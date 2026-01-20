package org.example.store.review.exception;

public class ReviewCreationException extends RuntimeException {
    public ReviewCreationException(String identifier) {
        super("Failed to create review '" + identifier + "'.");
    }
}
