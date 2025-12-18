package org.example.dao;

import org.example.model.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerDAO {

    Optional<Customer> findById(UUID customerId);

    Optional<Customer> findByEmail(String email);

    void save(Customer customer);

    void update(Customer customer);
}
