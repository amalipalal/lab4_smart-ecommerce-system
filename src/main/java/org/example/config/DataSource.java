package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
    private final HikariDataSource hikariDataSource;

    public DataSource(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        this.hikariDataSource = new HikariDataSource(config);
    }

    /**
     * Obtain a pooled JDBC Connection from the underlying HikariCP pool.
     *
     * The returned Connection is managed by HikariCP; see the HikariCP documentation for
     * pool tuning and lifecycle details: https://github.com/brettwooldridge/HikariCP
     *
     * @return a pooled {@link java.sql.Connection}
     * @throws SQLException if acquiring a connection from the pool fails
     * @see com.zaxxer.hikari.HikariDataSource
     */
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    /**
     * Close the underlying HikariDataSource and release pool resources.
     *
     * After calling this method the pool is shutdown and subsequent calls to {@link #getConnection()}
     * will fail.
     *
     * @see com.zaxxer.hikari.HikariDataSource#close()
     */
    public void close() {
        hikariDataSource.close();
    }
}
