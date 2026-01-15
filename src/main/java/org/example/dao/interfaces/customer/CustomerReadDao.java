package org.example.dao.interfaces.customer;

import org.example.dao.exception.DAOException;
import org.example.model.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerReadDao {

    Optional<Customer> findById(UUID customerId) throws DAOException;

    Optional<Customer> findByEmail(String email) throws DAOException;
}
