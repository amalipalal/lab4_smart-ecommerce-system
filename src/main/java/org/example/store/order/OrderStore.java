package org.example.store.order;

import org.example.application.ApplicationCache;
import org.example.config.DataSource;
import org.example.config.exception.DatabaseConnectionException;
import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.CustomerDao;
import org.example.dao.interfaces.OrdersDao;
import org.example.dao.interfaces.ProductDao;
import org.example.model.Customer;
import org.example.model.Orders;
import org.example.model.Product;
import org.example.store.order.exception.OrderPlacementException;
import org.example.store.order.exception.OrderRetrievalException;
import org.example.store.order.exception.OrderCountException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class OrderStore {
    private final DataSource dataSource;
    private final ApplicationCache cache;
    private final CustomerDao customerDao;
    private final ProductDao productDao;
    private final OrdersDao ordersDao;

    public OrderStore(DataSource dataSource, ApplicationCache cache, CustomerDao customerDao, ProductDao productDao, OrdersDao ordersDao) {
        this.dataSource = dataSource;
        this.cache = cache;
        this.customerDao = customerDao;
        this.productDao = productDao;
        this.ordersDao = ordersDao;
    }

    /**
     * Place an order within a single transaction.
     *
     * This method updates product stock via {@link org.example.dao.interfaces.ProductDao#update(java.sql.Connection, org.example.model.Product)},
     * ensures the customer exists via {@link org.example.dao.interfaces.CustomerDao#findById(java.sql.Connection, java.util.UUID)}
     * and creates the order via {@link org.example.dao.interfaces.OrdersDao#save(java.sql.Connection, org.example.model.Orders)}.
     *
     * @param order the order to persist
     * @param product the product with adjusted stock already applied
     * @param customer the customer placing the order
     * @throws org.example.store.order.exception.OrderPlacementException when any DAO operation fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
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
                throw new OrderPlacementException(order.getOrderId().toString());
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
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

    /**
     * Retrieve a page of orders.
     *
     * Delegates to {@link org.example.dao.interfaces.OrdersDao#getAllOrders(java.sql.Connection, int, int)}
     * and caches the result.
     *
     * @param limit maximum number of orders to return
     * @param offset zero-based offset for paging
     * @return list of {@link Orders}
     * @throws org.example.store.order.exception.OrderRetrievalException when DAO retrieval fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
    public List<Orders> getAllOrders(int limit, int offset) {
        try(Connection conn = dataSource.getConnection()) {
            String key = "order:all:" + limit + ':' + offset;
            return this.cache.getOrLoad(key, () -> this.ordersDao.getAllOrders(conn, limit, offset));
        } catch (DAOException e) {
            throw new OrderRetrievalException("all");
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public int countAll() {
        try(Connection conn = dataSource.getConnection()) {
            String key = "order:count";
            return cache.getOrLoad(key, () -> ordersDao.countAll(conn));
        } catch (DAOException e) {
            throw new OrderCountException("count");
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }
}
