package org.example.service;

import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.OrdersDAO;
import org.example.dao.interfaces.ProductDAO;
import org.example.dto.order.CustomerDetails;
import org.example.dto.order.OrderRequest;
import org.example.model.Orders;
import org.example.model.Product;
import org.example.util.data.DBConnection;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public class OrderService {
    private final OrdersDAO ordersDAO;
    private final ProductDAO productDAO;

    public OrderService(OrdersDAO ordersDAO, ProductDAO productDAO) {
        this.ordersDAO = ordersDAO;
        this.productDAO = productDAO;
    }

    public void placeOrder(OrderRequest orderRequest, CustomerDetails customerDetails) {
        try(Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            Product product = this.productDAO.findById(conn, orderRequest.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if(product.getStockQuantity() - orderRequest.quantity() < 0)
                throw new RuntimeException("Insufficient Stock");

            this.productDAO.reduceStock(conn, product.getProductId(), orderRequest.quantity());

            double totalPrice = product.getPrice() * orderRequest.quantity();
            Orders order = createOrder(orderRequest, null, totalPrice);  // TODO: Add customer id

            this.ordersDAO.save(conn, order);
            conn.commit();
        } catch (SQLException | DatabaseConnectionException | DAOException e) {
            throw new RuntimeException(e);
        }
    }

    private Orders createOrder(OrderRequest orderRequest, UUID customerId, double totalPrice) {
        return new Orders(
                UUID.randomUUID(),
                customerId,
                Instant.now(),
                totalPrice,
                orderRequest.shippingCountry(),
                orderRequest.shippingCity(),
                orderRequest.postalCode()
        );
    }
}
