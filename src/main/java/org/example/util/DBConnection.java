package org.example.util;

import org.example.config.DatabaseConfig;

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

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
        );
    }
}
