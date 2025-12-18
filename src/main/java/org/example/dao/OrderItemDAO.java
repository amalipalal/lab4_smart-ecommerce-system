package org.example.dao;

import org.example.model.OrderItem;

import java.util.List;
import java.util.UUID;

public interface OrderItemDAO {

    List<OrderItem> findByOrder(UUID orderId);

    void save(OrderItem orderItem);

    void saveAll(List<OrderItem> items);
}
