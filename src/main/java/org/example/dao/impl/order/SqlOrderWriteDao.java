package org.example.dao.impl.order;

import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.order.OrderWriteDao;
import org.example.model.Orders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SqlOrderWriteDao implements OrderWriteDao {

    private final Connection conn;

    private static final String SAVE = """
        INSERT INTO orders (
            order_id, customer_id, order_date, total_amount,
            shipping_country, shipping_city, shipping_postal_code
        )
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    public SqlOrderWriteDao(Connection connection) {
        this.conn = connection;
    }

    @Override
    public void save(Orders order) throws DAOException {
        try(PreparedStatement ps = conn.prepareStatement(SAVE)) {
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
