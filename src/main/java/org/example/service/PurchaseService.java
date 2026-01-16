package org.example.service;

import org.example.dto.order.CustomerDetails;
import org.example.dto.order.OrderRequest;
import org.example.store.OrderStore;

public class PurchaseService {
    private final OrderStore orderStore;

    public PurchaseService(OrderStore orderStore) {
        this.orderStore = orderStore;
    }

    public void purchaseProduct(OrderRequest orderRequest, CustomerDetails customerDetails) {
        this.orderStore.placeOrder(orderRequest, customerDetails);
    }
}
