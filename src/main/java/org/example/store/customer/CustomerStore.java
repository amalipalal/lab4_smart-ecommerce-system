package org.example.store.customer;

import org.example.application.ApplicationCache;
import org.example.config.DataSource;
import org.example.config.exception.DatabaseConnectionException;
import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.CustomerDao;
import org.example.model.Customer;
import org.example.store.customer.exception.CustomerCreationException;
import org.example.store.customer.exception.CustomerRetrievalException;
import org.example.store.customer.exception.CustomerSearchException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CustomerStore {
    private final DataSource dataSource;
    private final ApplicationCache cache;
    private final CustomerDao customerDao;

    public CustomerStore(DataSource dataSource, ApplicationCache cache, CustomerDao customerDao) {
        this.dataSource = dataSource;
        this.cache = cache;
        this.customerDao = customerDao;
    }

    public Optional<Customer> findByEmail(String email) {
        try(Connection conn = dataSource.getConnection()) {
            String key = "customer:" +  email;
            return this.cache.getOrLoad(key, () -> this.customerDao.findByEmail(conn, email));
        } catch (DAOException e) {
            throw new CustomerRetrievalException(email);
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public Optional<Customer> findById(UUID id) {
        try(Connection conn = dataSource.getConnection()) {
            String key = "customer:" + id;
            return this.cache.getOrLoad(key, () -> this.customerDao.findById(conn, id));
        } catch (DAOException e) {
            throw new CustomerRetrievalException(id.toString());
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public List<Customer> findByMultipleIds(Set<UUID> ids) {
        try(Connection conn = dataSource.getConnection()) {
            String key = "customer:multiple:" + ids.hashCode();
            return this.cache.getOrLoad(key, () -> this.customerDao.findByIds(conn, ids));
        } catch (DAOException e) {
            throw new CustomerSearchException("multiple:" + ids.hashCode());
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public void save(Customer customer) {
        try(Connection conn = dataSource.getConnection()) {
            this.customerDao.save(conn, customer);
        } catch (DAOException e) {
            throw new CustomerCreationException(customer.getCustomerId().toString());
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }
}
