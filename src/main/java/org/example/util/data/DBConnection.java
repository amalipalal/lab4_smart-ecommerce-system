package org.example.util.data;

import org.example.config.DatabaseConfig;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    static {
        try {
            Class.forName(DatabaseConfig.DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }
    }

    private DBConnection() {}

    public static Connection getConnection() throws DatabaseConnectionException {
        try {
            return DriverManager.getConnection(
                    DatabaseConfig.DB_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD
            );
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Failed to obtain database connection");
        }
    }

    public static void main(String[] args) {
        try(Connection conn = DBConnection.getConnection()) {
            System.out.println("DB connection successful");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
