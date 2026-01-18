package org.example.store;

import org.example.cache.ProductCache;
import org.example.config.DataSource;
import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.CustomerDao;
import org.example.model.Customer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    public Optional<Customer> findById(UUID id) {
        try(Connection conn = dataSource.getConnection()) {
            String key = "customer:" + id;
            return this.cache.getOrLoad(key, () -> this.customerDao.findById(conn, id));
        } catch (DAOException | SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Customer> findByMultipleIds(Set<UUID> ids) {
        try(Connection conn = dataSource.getConnection()) {
            String key = "customer:multiple:" + ids.hashCode();
            return this.cache.getOrLoad(key, () -> this.customerDao.findByIds(conn, ids));
        } catch (DAOException | SQLException e) {
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
