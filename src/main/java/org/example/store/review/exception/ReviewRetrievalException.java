package org.example.store.review.exception;

public class ReviewRetrievalException extends RuntimeException {
    public ReviewRetrievalException(String identifier) {
        super("Failed to retrieve '" + identifier + "'.");
    }
}
