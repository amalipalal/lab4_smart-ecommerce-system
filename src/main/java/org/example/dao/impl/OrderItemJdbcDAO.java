package org.example.dao.impl;

import org.example.config.DBConnection;
import org.example.dao.interfaces.OrderItemDAO;
import org.example.dao.interfaces.StatementPreparer;
import org.example.dao.exception.DAOException;
import org.example.model.OrderItem;
import org.example.config.exception.DatabaseConnectionException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderItemJdbcDAO implements OrderItemDAO {

    private static final String FIND_BY_ORDER = """
        SELECT * FROM order_item
        WHERE order_id = ?
        """;

    private static final String SAVE = """
        INSERT INTO order_item (
            order_item_id, order_id, product_id,
            quantity, price_at_purchase
        )
        VALUES (?, ?, ?, ?, ?)
        """;

    @Override
    public List<OrderItem> findByOrder(UUID orderId) throws DAOException {
        List<OrderItem> items = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ORDER)) {

            ps.setObject(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(map(rs));
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to fetch order items for order " + orderId, e);
        }
        return items;
    }

    @Override
    public void save(OrderItem item) throws DAOException {
        try {
            insertionQuery(SAVE, ps -> prepare(ps, item));
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to save order item", e);
        }
    }

    @Override
    public void saveAll(List<OrderItem> items) throws DAOException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SAVE)) {

            for (OrderItem item : items) {
                prepare(ps, item);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to batch insert order items", e);
        }
    }

    private OrderItem map(ResultSet rs) throws SQLException {
        return new OrderItem(
                rs.getObject("order_item_id", UUID.class),
                rs.getObject("order_id", UUID.class),
                rs.getObject("product_id", UUID.class),
                rs.getInt("quantity"),
                rs.getDouble("price_at_purchase")
        );
    }

    private void prepare(PreparedStatement ps, OrderItem item) throws SQLException {
        ps.setObject(1, item.getOrderItemId());
        ps.setObject(2, item.getOrderId());
        ps.setObject(3, item.getProductId());
        ps.setInt(4, item.getQuantity());
        ps.setDouble(5, item.getPriceAtPurchase());
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
