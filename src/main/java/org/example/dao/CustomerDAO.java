package org.example.dao;

import org.example.dao.exception.DAOException;
import org.example.model.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerDAO {

    Optional<Customer> findById(UUID customerId) throws DAOException;

    Optional<Customer> findByEmail(String email) throws DAOException;

    void save(Customer customer) throws DAOException;

    void update(Customer customer) throws DAOException;
}
