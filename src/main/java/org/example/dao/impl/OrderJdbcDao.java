package org.example.dao.impl;

import org.example.dao.interfaces.OrdersDao;
import org.example.dao.exception.DAOException;
import org.example.model.Orders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderJdbcDao implements OrdersDao {
    private static final String FIND_BY_ID = """
        SELECT order_id, customer_id, order_date, total_amount,
               shipping_country, shipping_city, shipping_postal_code
        FROM orders
        WHERE order_id = ?
        """;

    private static final String ALL_ORDERS = """
        SELECT order_id, customer_id, order_date, total_amount,
               shipping_country, shipping_city, shipping_postal_code
        FROM orders
        ORDER BY order_date DESC
        LIMIT ? OFFSET ?
        """;

    private static final String COUNT = """
        SELECT COUNT(*) FROM orders
        """;

    private static final String FIND_BY_CUSTOMER = """
        SELECT order_id, customer_id, order_date, total_amount,
               shipping_country, shipping_city, shipping_postal_code
        FROM orders
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
    public Optional<Orders> findById(Connection conn, UUID orderId) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {
            ps.setObject(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
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
    public List<Orders> getAllOrders(Connection connection, int limit, int offset) throws DAOException {
        List<Orders> orders = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(ALL_ORDERS)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try(ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(map(resultSet));
                }
            }
        } catch ( SQLException e) {
            throw new DAOException("Failed to search orders by name", e);
        }

        return orders;
    }

    @Override
    public int countAll(Connection conn) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(COUNT)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to count orders by name", e);
        }
    }

    @Override
    public void save(Connection conn, Orders order) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(SAVE)) {
            ps.setObject(1, order.getOrderId());
            ps.setObject(2, order.getCustomerId());
            ps.setTimestamp(3, Timestamp.from(order.getOrderDate()));
            ps.setDouble(4, order.getTotalAmount());
            ps.setString(5, order.getShippingCountry());
            ps.setString(6, order.getShippingCity());
            ps.setString(7, order.getShippingPostalCode());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Failed to save order " + order.getOrderId(), e);
        }
    }
}
