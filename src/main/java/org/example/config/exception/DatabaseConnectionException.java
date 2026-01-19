package org.example.config.exception;

public class DatabaseConnectionException extends RuntimeException {
    public DatabaseConnectionException(Throwable cause) {
        super(cause);
    }
}
