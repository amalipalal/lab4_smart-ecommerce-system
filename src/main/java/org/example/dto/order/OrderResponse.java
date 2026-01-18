package org.example.dto.order;

import org.example.model.Customer;
import org.example.model.Orders;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String customerEmail,
        Instant orderDate,
        double totalAmount,
        String shippingCountry,
        String shippingCity,
        String shippingPostalCode
) {
    public OrderResponse(Orders order, String customerEmail) {
        this(
            order.getOrderId(),
            customerEmail,
            order.getOrderDate(),
            order.getTotalAmount(),
            order.getShippingCountry(),
            order.getShippingCity(),
            order.getShippingPostalCode()
        );
    }
}
