package org.example.dao.impl;

import org.example.dao.OrdersDAO;
import org.example.dao.StatementPreparer;
import org.example.dao.exception.DAOException;
import org.example.model.Orders;
import org.example.util.DBConnection;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderJdbcDAO implements OrdersDAO {
    private static final String FIND_BY_ID = """
        SELECT * FROM orders
        WHERE order_id = ?
        """;

    private static final String FIND_BY_CUSTOMER = """
        SELECT * FROM orders
        WHERE customer_id = ?
        ORDER BY order_date DESC
        LIMIT ? OFFSET ?
        """;

    private static final String SAVE = """
        INSERT INTO orders (
            order_id, customer_id, order_date, total_amount,
            shipping_country, shipping_city, shipping_postal_code
        )
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    @Override
    public Optional<Orders> findById(UUID orderId) throws DAOException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setObject(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to fetch order " + orderId, e);
        }
        return Optional.empty();
    }

    private Orders map(ResultSet rs) throws SQLException {
        return new Orders(
                rs.getObject("order_id", UUID.class),
                rs.getObject("customer_id", UUID.class),
                rs.getTimestamp("order_date").toInstant(),
                rs.getDouble("total_amount"),
                rs.getString("shipping_country"),
                rs.getString("shipping_city"),
                rs.getString("shipping_postal_code")
        );
    }

    @Override
    public List<Orders> findByCustomer(UUID customerId, int limit, int offset) throws DAOException {
        List<Orders> orders = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_CUSTOMER)) {

            ps.setObject(1, customerId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(map(rs));
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to fetch orders for customer " + customerId, e);
        }
        return orders;
    }

    @Override
    public void save(Orders order) throws DAOException {
        try {
            insertionQuery(SAVE, ps -> {
                ps.setObject(1, order.getOrderId());
                ps.setObject(2, order.getCustomerId());
                ps.setTimestamp(3, Timestamp.from(order.getOrderDate()));
                ps.setDouble(4, order.getTotalAmount());
                ps.setString(5, order.getShippingCountry());
                ps.setString(6, order.getShippingCity());
                ps.setString(7, order.getShippingPostalCode());
            });
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to save order " + order.getOrderId(), e);
        }
    }

    private void insertionQuery(String sql, StatementPreparer preparer)
            throws SQLException, DatabaseConnectionException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preparer.prepare(ps);
            ps.executeUpdate();
        }
    }
}
