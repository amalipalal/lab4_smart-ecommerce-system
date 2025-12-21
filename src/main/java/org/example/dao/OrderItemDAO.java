package org.example.dao;

import org.example.dao.exception.DAOException;
import org.example.model.OrderItem;

import java.util.List;
import java.util.UUID;

public interface OrderItemDAO {

    List<OrderItem> findByOrder(UUID orderId) throws DAOException;

    void save(OrderItem orderItem) throws DAOException;

    void saveAll(List<OrderItem> items) throws DAOException;
}
