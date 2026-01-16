package org.example.store;

import org.example.cache.ProductCache;
import org.example.config.DataSource;
import org.example.dao.interfaces.CustomerDao;
import org.example.dao.interfaces.OrdersDao;
import org.example.dao.interfaces.ProductDao;
import org.example.dto.order.CustomerDetails;
import org.example.dto.order.OrderRequest;
import org.example.model.Customer;
import org.example.model.Orders;
import org.example.model.Product;
import org.example.service.exception.InsufficientProductStock;
import org.example.service.exception.ProductNotFoundException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class OrderStore {
    private final DataSource dataSource;
    private final ProductCache cache;
    private final CustomerDao customerDao;
    private final ProductDao productDao;
    private final OrdersDao ordersDao;

    public OrderStore(DataSource dataSource, ProductCache cache, CustomerDao customerDao, ProductDao productDao, OrdersDao ordersDao) {
        this.dataSource = dataSource;
        this.cache = cache;
        this.customerDao = customerDao;
        this.productDao = productDao;
        this.ordersDao = ordersDao;
    }

    public void placeOrder(OrderRequest orderRequest, CustomerDetails customerDetails) {
        try(Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Product product = retrieveProduct(conn, orderRequest);
                validateStock(product, orderRequest.quantity());

                this.productDao.reduceStock(conn, product.getProductId(), orderRequest.quantity());

                Customer customer = createOrFindCustomer(conn, customerDetails);

                Orders order = createOrder(orderRequest, customer.getCustomerId(), product);
                this.ordersDao.save(conn, order);

                conn.commit();
                invalidateCache(product.getProductId());
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                throw new RuntimeException("Failed to create product.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error");
        }
    }

    private Product retrieveProduct(Connection conn, OrderRequest orderRequest) {
        UUID productId = orderRequest.productId();
        String key = "product:" + productId.toString();
        return this.cache.getOrLoad(key, () -> this.productDao.findById(conn, productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString())));
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new InsufficientProductStock(product.getProductId().toString());
        }
    }

    private Customer createOrFindCustomer(Connection conn, CustomerDetails customerDetails) {
        String key = "customer:" + customerDetails.email();
        Optional<Customer> customer = this.cache.getOrLoad(key, () -> customerDao.findByEmail(conn, customerDetails.email()));

        if (customer.isPresent())
            return customer.get();
        Customer newCustomer = new Customer(
                UUID.randomUUID(),
                customerDetails.firstName(),
                customerDetails.lastName(),
                customerDetails.email(),
                customerDetails.phone(),
                Instant.now()
        );
        this.customerDao.save(conn, newCustomer);
        return newCustomer;
    }

    private Orders createOrder(OrderRequest orderRequest, UUID customerId, Product product) {
        double totalPrice = product.getPrice() * orderRequest.quantity();
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

    private void invalidateCache(UUID productId) {
        this.cache.invalidate("product:" + productId);
        this.cache.invalidateByPrefix("product:all:");
        this.cache.invalidateByPrefix("product:search:");
        this.cache.invalidateByPrefix("product:count:");
    }
}
