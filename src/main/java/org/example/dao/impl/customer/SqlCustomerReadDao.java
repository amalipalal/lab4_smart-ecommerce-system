package org.example.dao.impl.customer;

import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.customer.CustomerReadDao;
import org.example.model.Customer;
import org.example.util.data.DBConnection;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class SqlCustomerReadDao implements CustomerReadDao {

    private static final String FIND_BY_ID = """
            SELECT * FROM customer
            WHERE customer_id = ?
            """;

    private static final String FIND_BY_EMAIL = """
            SELECT * FROM customer
            WHERE email = ?
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
}
