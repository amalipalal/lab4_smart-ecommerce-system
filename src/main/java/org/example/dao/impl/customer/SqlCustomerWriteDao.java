package org.example.dao.impl.customer;

import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.customer.CustomerWriteDao;
import org.example.model.Customer;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SqlCustomerWriteDao implements CustomerWriteDao {

    private final Connection conn;

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

    public SqlCustomerWriteDao(Connection connection) {
        this.conn = connection;
    }

    @Override
    public void save(Customer customer) throws DAOException {
        try(PreparedStatement ps = conn.prepareStatement(SAVE)) {
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
        try(PreparedStatement ps = conn.prepareStatement(UPDATE);) {
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
