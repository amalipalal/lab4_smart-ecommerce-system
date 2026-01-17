package org.example.service;

import org.example.dto.order.CustomerDetails;
import org.example.dto.order.OrderRequest;
import org.example.model.Customer;
import org.example.model.Orders;
import org.example.model.Product;
import org.example.service.exception.InsufficientProductStock;
import org.example.service.exception.ProductNotFoundException;
import org.example.store.CustomerStore;
import org.example.store.OrderStore;
import org.example.store.ProductStore;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class PurchaseService {
    private final OrderStore orderStore;
    private final ProductStore productStore;
    private final CustomerStore customerStore;

    public PurchaseService(OrderStore orderStore, ProductStore productStore, CustomerStore customerStore) {
        this.orderStore = orderStore;
        this.productStore = productStore;
        this.customerStore = customerStore;
    }

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
}
