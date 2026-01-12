package org.example.dao.impl;

import org.example.dao.interfaces.CustomerDAO;
import org.example.dao.exception.DAOException;
import org.example.model.Customer;
import org.example.util.data.DBConnection;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class CustomerJdbcDAO implements CustomerDAO {

    private static final String FIND_BY_ID = """
            SELECT * FROM customer
            WHERE customer_id = ?
            """;

    private static final String FIND_BY_EMAIL = """
            SELECT * FROM customer
            WHERE email = ?
            """;

    private static final String SAVE = """
            INSERT INTO customer
            (customer_id, first_name, last_name, email, phone, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE customer
            SET first_name = ?, last_name = ?, phone = ?
            WHERE customer_id = ?
            """;

    @Override
    public Optional<Customer> findById(UUID customerId) throws DAOException {
        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(FIND_BY_ID);
            ps.setObject(1, customerId);

            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private Customer map(ResultSet resultSet) throws SQLException {
        return new Customer(
                resultSet.getObject("customer_id", UUID.class),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                resultSet.getTimestamp("created_at").toInstant()
        );
    }

    @Override
    public Optional<Customer> findByEmail(String email) throws DAOException {
        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(FIND_BY_EMAIL);
            ps.setString(1, email);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to find customer with email: " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Customer customer) throws DAOException {
        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SAVE);

            ps.setObject(1, customer.getCustomerId());
            ps.setString(2, customer.getFirstName());
            ps.setString(3, customer.getLastName());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getPhone());
            ps.setTimestamp(6, Timestamp.from(customer.getCreatedAt()));

            ps.executeUpdate();
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to save customer " + customer.getCustomerId(), e);
        }
    }

    @Override
    public void update(Customer customer) throws DAOException {
        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(UPDATE);

            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getPhone());
            ps.setObject(4, customer.getCustomerId());

            ps.executeUpdate();
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to save customer " + customer.getCustomerId(), e);
        }
    }
}
