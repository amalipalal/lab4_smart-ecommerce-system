package org.example.persistence;

import org.example.util.exception.DatabaseConnectionException;

import java.sql.Connection;

public interface UnitOfWork {
    Connection getConnection();
    void commit() throws DatabaseConnectionException;
    void rollback() throws DatabaseConnectionException;
    void close() throws DatabaseConnectionException;
}
