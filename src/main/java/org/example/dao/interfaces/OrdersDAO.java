package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Orders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdersDAO {

    Optional<Orders> findById(UUID orderId) throws DAOException;

    List<Orders> findByCustomer(UUID customerId, int limit, int offset) throws DAOException;

    void save(Orders order) throws DAOException;
}
