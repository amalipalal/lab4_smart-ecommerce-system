package org.example.dao;

import org.example.model.Orders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdersDAO {

    Optional<Orders> findById(UUID orderId);

    List<Orders> findByCustomer(UUID customerId, int limit, int offset);

    void save(Orders order);
}
