package org.example.model;

import java.time.Instant;
import java.util.UUID;

public class Orders {
    private UUID orderId;
    private UUID customerId;
    private Instant orderDate;
    private double totalAmount;
    private String shippingCountry;
    private String shippingCity;
    private String shippingPostalCode;

    public Orders() {}

    public Orders(UUID orderId, UUID customerId, Instant orderDate, double totalAmount,
                  String shippingCountry, String shippingCity, String shippingPostalCode
    ) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.shippingCountry = shippingCountry;
        this.shippingCity = shippingCity;
        this.shippingPostalCode = shippingPostalCode;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Instant orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingCountry() {
        return shippingCountry;
    }

    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }

    public String getShippingPostalCode() {
        return shippingPostalCode;
    }

    public void setShippingPostalCode(String shippingPostalCode) {
        this.shippingPostalCode = shippingPostalCode;
    }
}
