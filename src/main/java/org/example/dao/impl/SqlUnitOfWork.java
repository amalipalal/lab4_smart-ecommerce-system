package org.example.dao.impl;

import org.example.dao.interfaces.UnitOfWork;
import org.example.util.data.DBConnection;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlUnitOfWork implements UnitOfWork {
    private final Connection conn;

    public SqlUnitOfWork() throws DatabaseConnectionException {
        try {
            this.conn = DBConnection.getConnection();
            this.conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Failed to get connection");
        }
    }

    public Connection getConnection() { return conn; }

    public void commit() throws DatabaseConnectionException {
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Could not commit change");
        }
    }

    public void rollback() throws DatabaseConnectionException {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Could not rollback change");
        }
    }

    public void close() throws DatabaseConnectionException {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Could not commit change");
        }
    }
}
