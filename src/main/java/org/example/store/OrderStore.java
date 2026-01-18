package org.example.store;

import org.example.cache.ProductCache;
import org.example.config.DataSource;
import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.CustomerDao;
import org.example.dao.interfaces.OrdersDao;
import org.example.dao.interfaces.ProductDao;
import org.example.model.Customer;
import org.example.model.Orders;
import org.example.model.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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

    public void placeOrder(Orders order, Product product, Customer customer) {
        try(Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                this.productDao.update(conn, product);
                // Check if the customer making order should be saved in db or not
                if(this.customerDao.findById(conn, customer.getCustomerId()).isEmpty())
                    this.customerDao.save(conn, customer);

                this.ordersDao.save(conn, order);

                conn.commit();
                invalidateCache(product.getProductId());
            } catch (DAOException e) {
                conn.rollback();
                e.printStackTrace();
                throw new RuntimeException("Failed to place order");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error", e);
        }
    }

    private void invalidateCache(UUID productId) {
        this.cache.invalidate("product:" + productId);
        this.cache.invalidateByPrefix("product:all:");
        this.cache.invalidateByPrefix("product:search:");
        this.cache.invalidateByPrefix("product:count:");
        this.cache.invalidateByPrefix("order:all:");
        this.cache.invalidateByPrefix("order:count");
    }

    public List<Orders> getAllOrders(int limit, int offset) {
        try(Connection conn = dataSource.getConnection()) {
            String key = "order:all:" + limit + ':' + offset;
            return this.cache.getOrLoad(key, () -> this.ordersDao.getAllOrders(conn, limit, offset));
        } catch (DAOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to search orders", e);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database connection error", e);
        }
    }

    public int countAll() {
        try(Connection conn = dataSource.getConnection()) {
            String key = "order:count";
            return cache.getOrLoad(key, () -> ordersDao.countAll(conn));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error", e);
        }
    }
}
