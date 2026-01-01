package org.example.dao.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String string) {
        super(string);
    }
}
