package org.example.store.product.exception;

public class DeleteProductException extends RuntimeException {
    public DeleteProductException(String identifier) {
        super("Failed to delete the product '" + identifier + "'.");
    }
}
