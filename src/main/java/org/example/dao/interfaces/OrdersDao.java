package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Orders;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdersDao {

    Optional<Orders> findById(Connection connection, UUID orderId) throws DAOException;

    List<Orders> findByCustomer(Connection connection, UUID customerId, int limit, int offset) throws DAOException;

    void save(Connection connection, Orders order) throws DAOException;
}
