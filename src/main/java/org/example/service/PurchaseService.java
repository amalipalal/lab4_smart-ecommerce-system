package org.example.service;

import org.example.dto.order.CustomerDetails;
import org.example.dto.order.OrderRequest;
import org.example.dto.order.OrderResponse;
import org.example.model.Customer;
import org.example.model.Orders;
import org.example.model.Product;
import org.example.service.exception.InsufficientProductStock;
import org.example.service.exception.ProductNotFoundException;
import org.example.store.customer.CustomerStore;
import org.example.store.order.OrderStore;
import org.example.store.product.ProductStore;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PurchaseService {
    private final OrderStore orderStore;
    private final ProductStore productStore;
    private final CustomerStore customerStore;

    public PurchaseService(OrderStore orderStore, ProductStore productStore, CustomerStore customerStore) {
        this.orderStore = orderStore;
        this.productStore = productStore;
        this.customerStore = customerStore;
    }

    /**
     * Process a purchase request.
     *
     * This method:
     * - Loads the product using {@link ProductStore#getProduct(java.util.UUID)} and validates stock.
     * - Creates or finds the customer via {@link CustomerStore#findByEmail(String)}.
     * - Updates product stock and places an order through {@link OrderStore#placeOrder(Orders, org.example.model.Product, org.example.model.Customer)}.
     *
     * @param orderRequest    the incoming {@link OrderRequest} with product id, quantity and shipping info
     * @param customerDetails the purchaser details {@link CustomerDetails}
     * @throws ProductNotFoundException   if the product cannot be found
     * @throws InsufficientProductStock   if product stock is lower than requested quantity
     */
    public void purchaseProduct(OrderRequest orderRequest, CustomerDetails customerDetails) {
        UUID productId = orderRequest.productId();
        Product product = this.productStore.getProduct(productId).orElseThrow(
                () -> new ProductNotFoundException(productId.toString()));

        if(product.getStockQuantity() < orderRequest.quantity())
            throw new InsufficientProductStock(productId.toString());

        Customer customer = createOrFindCustomer(customerDetails);
        Product updatedProduct = createUpdatedProduct(product, orderRequest.quantity());
        Orders order = createOrder(orderRequest, customer.getCustomerId(), updatedProduct);

        this.orderStore.placeOrder(order, updatedProduct, customer);
    }

    private Product createUpdatedProduct(Product product, int quantityBought) {
        return new Product(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity() - quantityBought,
                product.getCategoryId(),
                product.getCreatedAt(),
                Instant.now()
        );
    }

    private Customer createOrFindCustomer(CustomerDetails customerDetails) {
        Optional<Customer> customer = this.customerStore.findByEmail(customerDetails.email());

        if(customer.isPresent())
            return customer.get();
        return new Customer(
                UUID.randomUUID(),
                customerDetails.firstName(),
                customerDetails.lastName(),
                customerDetails.email(),
                customerDetails.phone(),
                Instant.now()
        );
    }

    private Orders createOrder(OrderRequest orderRequest, UUID customerId, Product product) {
        double totalPrice = product.getPrice() * orderRequest.quantity();
        return new Orders(
                UUID.randomUUID(),
                customerId,
                Instant.now(),
                totalPrice,
                orderRequest.shippingCountry(),
                orderRequest.shippingCity(),
                orderRequest.postalCode()
        );
    }

    /**
     * Retrieve paged purchase history.
     *
     * Fetches orders from {@link OrderStore#getAllOrders(int, int)}, batch-loads customers via
     * {@link CustomerStore#findByMultipleIds(java.util.Set)}, and returns {@link OrderResponse} entries.
     *
     * @param limit  maximum number of orders to return
     * @param offset zero-based offset for paging
     * @return list of {@link OrderResponse}
     */
    public List<OrderResponse> getPurchaseHistory(int limit, int offset) {
        List<Orders> orders = this.orderStore.getAllOrders(limit, offset);
        Set<UUID> customerIds = orders.stream()
                .map(Orders::getCustomerId)
                .collect(Collectors.toSet());

        // Batch fetch specified customers from db to prevent N + 1
        Map<UUID, Customer> customerMap = this.customerStore.findByMultipleIds(customerIds)
                .stream()
                .collect(Collectors.toMap(Customer::getCustomerId, Function.identity()));

        return orders.stream()
                .map(order -> {
                    String customerEmail = customerMap.get(order.getCustomerId()).getEmail();
                    return new OrderResponse(order, customerEmail);
                }).toList();
    }

    public int countPurchases() {
        return this.orderStore.countAll();
    }

}
