package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Customer;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CustomerDao {

    Optional<Customer> findById(Connection connection, UUID customerId) throws DAOException;

    List<Customer> findByIds(Connection connection, Set<UUID> customerIds) throws DAOException;

    Optional<Customer> findByEmail(Connection connection, String email) throws DAOException;

    void save(Connection connection, Customer customer) throws DAOException;

    void update(Connection connection, Customer customer) throws DAOException;
}
