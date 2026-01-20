package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Customer;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CustomerDao {

    /**
     * Find a customer by id.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param customerId customer identifier
     * @return optional customer when found
     * @throws DAOException on DAO errors
     */
    Optional<Customer> findById(Connection connection, UUID customerId) throws DAOException;

    /**
     * Find multiple customers by a set of ids.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param customerIds set of customer identifiers
     * @return list of found customers
     * @throws DAOException on DAO errors
     */
    List<Customer> findByIds(Connection connection, Set<UUID> customerIds) throws DAOException;

    /**
     * Find a customer by email.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param email customer email
     * @return optional customer when found
     * @throws DAOException on DAO errors
     */
    Optional<Customer> findByEmail(Connection connection, String email) throws DAOException;

    /**
     * Persist a new {@link Customer}.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param customer customer to save
     * @throws DAOException on DAO errors
     */
    void save(Connection connection, Customer customer) throws DAOException;

    /**
     * Update an existing {@link Customer}.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param customer customer to update
     * @throws DAOException on DAO errors
     */
    void update(Connection connection, Customer customer) throws DAOException;
}
