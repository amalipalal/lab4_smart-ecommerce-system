package org.example.dao.interfaces.customer;

import org.example.dao.exception.DAOException;
import org.example.model.Customer;

public interface CustomerWriteDao {

    void save(Customer customer) throws DAOException;

    void update(Customer customer) throws DAOException;
}
