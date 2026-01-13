package org.example.dao.interfaces;

import org.example.util.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.SQLException;

public interface UnitOfWork {
    Connection getConnection();
    void commit() throws DatabaseConnectionException;
    void rollback() throws DatabaseConnectionException;
    void close() throws DatabaseConnectionException;
}
