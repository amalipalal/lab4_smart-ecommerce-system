package org.example.store;

import org.example.cache.ProductCache;
import org.example.config.DataSource;
import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.CustomerDao;
import org.example.model.Customer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class CustomerStore {
    private final DataSource dataSource;
    private final ProductCache cache;
    private final CustomerDao customerDao;

    public CustomerStore(DataSource dataSource, ProductCache cache, CustomerDao customerDao) {
        this.dataSource = dataSource;
        this.cache = cache;
        this.customerDao = customerDao;
    }

    public Optional<Customer> findByEmail(String email) {
        try(Connection conn = dataSource.getConnection()) {
            String key = "customer:" +  email;
            return this.cache.getOrLoad(key, () -> this.customerDao.findByEmail(conn, email));
        } catch (SQLException | DAOException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public void save(Customer customer) {
        try(Connection conn = dataSource.getConnection()) {
            this.customerDao.save(conn, customer);
        } catch (DAOException | SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }
}
