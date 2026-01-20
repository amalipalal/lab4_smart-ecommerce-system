import org.example.dto.order.CustomerDetails;
import org.example.dto.order.OrderRequest;
import org.example.dto.order.OrderResponse;
import org.example.model.Customer;
import org.example.model.Orders;
import org.example.model.Product;
import org.example.service.PurchaseService;
import org.example.service.exception.InsufficientProductStock;
import org.example.service.exception.ProductNotFoundException;
import org.example.store.customer.CustomerStore;
import org.example.store.order.OrderStore;
import org.example.store.product.ProductStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private OrderStore orderStore;

    @Mock
    private ProductStore productStore;

    @Mock
    private CustomerStore customerStore;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    @DisplayName("Should purchase product successfully for existing customer")
    void shouldPurchaseProductSuccessfullyForExistingCustomer() {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OrderRequest orderRequest = new OrderRequest(
                productId, 2, "Ghana", "Accra", "00233", "12345"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );
        Product product = new Product(
                productId, "Laptop", "Gaming Laptop", 1500.0, 10,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );
        Customer customer = new Customer(
                customerId, "John", "Doe", "john@example.com", "+233123456789", Instant.now()
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.of(product));
        when(customerStore.findByEmail("john@example.com")).thenReturn(Optional.of(customer));

        purchaseService.purchaseProduct(orderRequest, customerDetails);

        verify(productStore).getProduct(productId);
        verify(customerStore).findByEmail("john@example.com");
        verify(orderStore).placeOrder(any(Orders.class), any(Product.class), eq(customer));
    }

    @Test
    @DisplayName("Should purchase product successfully for new customer")
    void shouldPurchaseProductSuccessfullyForNewCustomer() {
        UUID productId = UUID.randomUUID();
        OrderRequest orderRequest = new OrderRequest(
                productId, 1, "Ghana", "Kumasi", "00233", "54321"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "Jane", "Smith", "jane@example.com", "+233987654321"
        );
        Product product = new Product(
                productId, "Phone", "Smartphone", 800.0, 5,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.of(product));
        when(customerStore.findByEmail("jane@example.com")).thenReturn(Optional.empty());

        purchaseService.purchaseProduct(orderRequest, customerDetails);

        verify(productStore).getProduct(productId);
        verify(customerStore).findByEmail("jane@example.com");
        verify(orderStore).placeOrder(any(Orders.class), any(Product.class), argThat(customer ->
                customer.getFirstName().equals("Jane") &&
                customer.getLastName().equals("Smith") &&
                customer.getEmail().equals("jane@example.com")
        ));
    }

    @Test
    @DisplayName("Should throw error when product not found")
    void shouldThrowWhenProductNotFound() {
        UUID productId = UUID.randomUUID();
        OrderRequest orderRequest = new OrderRequest(
                productId, 1, "Ghana", "Accra", "00233", "12345"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> purchaseService.purchaseProduct(orderRequest, customerDetails)
        );

        verify(productStore).getProduct(productId);
        verify(orderStore, never()).placeOrder(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw error when insufficient stock")
    void shouldThrowWhenInsufficientStock() {
        UUID productId = UUID.randomUUID();
        OrderRequest orderRequest = new OrderRequest(
                productId, 10, "Ghana", "Accra", "00233", "12345"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );
        Product product = new Product(
                productId, "Laptop", "Gaming Laptop", 1500.0, 5,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.of(product));

        Assertions.assertThrows(
                InsufficientProductStock.class,
                () -> purchaseService.purchaseProduct(orderRequest, customerDetails)
        );

        verify(productStore).getProduct(productId);
        verify(orderStore, never()).placeOrder(any(), any(), any());
    }

    @Test
    @DisplayName("Should reduce stock quantity correctly")
    void shouldReduceStockQuantityCorrectly() {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OrderRequest orderRequest = new OrderRequest(
                productId, 3, "Ghana", "Accra", "00233", "12345"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );
        Product product = new Product(
                productId, "Laptop", "Gaming Laptop", 1500.0, 10,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );
        Customer customer = new Customer(
                customerId, "John", "Doe", "john@example.com", "+233123456789", Instant.now()
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.of(product));
        when(customerStore.findByEmail("john@example.com")).thenReturn(Optional.of(customer));

        purchaseService.purchaseProduct(orderRequest, customerDetails);

        verify(orderStore).placeOrder(any(Orders.class), argThat(updatedProduct ->
                updatedProduct.getStockQuantity() == 7
        ), eq(customer));
    }

    @Test
    @DisplayName("Should calculate total price correctly")
    void shouldCalculateTotalPriceCorrectly() {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        double price = 500.0;
        int quantity = 4;
        OrderRequest orderRequest = new OrderRequest(
                productId, quantity, "Ghana", "Accra", "00233", "12345"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );
        Product product = new Product(
                productId, "Phone", "Smartphone", price, 10,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );
        Customer customer = new Customer(
                customerId, "John", "Doe", "john@example.com", "+233123456789", Instant.now()
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.of(product));
        when(customerStore.findByEmail("john@example.com")).thenReturn(Optional.of(customer));

        purchaseService.purchaseProduct(orderRequest, customerDetails);

        verify(orderStore).placeOrder(argThat(order ->
                order.getTotalAmount() == price * quantity
        ), any(Product.class), eq(customer));
    }

    @Test
    @DisplayName("Should create order with correct shipping details")
    void shouldCreateOrderWithCorrectShippingDetails() {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OrderRequest orderRequest = new OrderRequest(
                productId, 1, "Ghana", "Tamale", "00233", "98765"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );
        Product product = new Product(
                productId, "Tablet", "Android Tablet", 300.0, 8,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );
        Customer customer = new Customer(
                customerId, "John", "Doe", "john@example.com", "+233123456789", Instant.now()
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.of(product));
        when(customerStore.findByEmail("john@example.com")).thenReturn(Optional.of(customer));

        purchaseService.purchaseProduct(orderRequest, customerDetails);

        verify(orderStore).placeOrder(argThat(order ->
                order.getShippingCountry().equals("Ghana") &&
                order.getShippingCity().equals("Tamale") &&
                order.getShippingPostalCode().equals("98765")
        ), any(Product.class), eq(customer));
    }

    @Test
    @DisplayName("Should handle exact stock quantity purchase")
    void shouldHandleExactStockQuantityPurchase() {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OrderRequest orderRequest = new OrderRequest(
                productId, 5, "Ghana", "Accra", "00233", "12345"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );
        Product product = new Product(
                productId, "Laptop", "Gaming Laptop", 1500.0, 5,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );
        Customer customer = new Customer(
                customerId, "John", "Doe", "john@example.com", "+233123456789", Instant.now()
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.of(product));
        when(customerStore.findByEmail("john@example.com")).thenReturn(Optional.of(customer));

        purchaseService.purchaseProduct(orderRequest, customerDetails);

        verify(orderStore).placeOrder(any(Orders.class), argThat(updatedProduct ->
                updatedProduct.getStockQuantity() == 0
        ), eq(customer));
    }

    @Test
    @DisplayName("Should throw error when stock is zero")
    void shouldThrowWhenStockIsZero() {
        UUID productId = UUID.randomUUID();
        OrderRequest orderRequest = new OrderRequest(
                productId, 1, "Ghana", "Accra", "00233", "12345"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );
        Product product = new Product(
                productId, "Laptop", "Gaming Laptop", 1500.0, 0,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.of(product));

        Assertions.assertThrows(
                InsufficientProductStock.class,
                () -> purchaseService.purchaseProduct(orderRequest, customerDetails)
        );

        verify(orderStore, never()).placeOrder(any(), any(), any());
    }

    @Test
    @DisplayName("Should preserve product properties when updating stock")
    void shouldPreserveProductPropertiesWhenUpdatingStock() {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(86400);
        OrderRequest orderRequest = new OrderRequest(
                productId, 2, "Ghana", "Accra", "00233", "12345"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );
        Product product = new Product(
                productId, "Laptop", "Gaming Laptop", 1500.0, 10,
                categoryId, createdAt, Instant.now()
        );
        Customer customer = new Customer(
                customerId, "John", "Doe", "john@example.com", "+233123456789", Instant.now()
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.of(product));
        when(customerStore.findByEmail("john@example.com")).thenReturn(Optional.of(customer));

        purchaseService.purchaseProduct(orderRequest, customerDetails);

        verify(orderStore).placeOrder(any(Orders.class), argThat(updatedProduct ->
                updatedProduct.getProductId().equals(productId) &&
                updatedProduct.getName().equals("Laptop") &&
                updatedProduct.getDescription().equals("Gaming Laptop") &&
                updatedProduct.getPrice() == 1500.0 &&
                updatedProduct.getCategoryId().equals(categoryId) &&
                updatedProduct.getCreatedAt().equals(createdAt)
        ), eq(customer));
    }

    @Test
    @DisplayName("Should create new customer with correct details")
    void shouldCreateNewCustomerWithCorrectDetails() {
        UUID productId = UUID.randomUUID();
        OrderRequest orderRequest = new OrderRequest(
                productId, 1, "Ghana", "Accra", "00233", "12345"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "Alice", "Wonder", "alice@example.com", "+233111222333"
        );
        Product product = new Product(
                productId, "Phone", "Smartphone", 800.0, 5,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.of(product));
        when(customerStore.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        purchaseService.purchaseProduct(orderRequest, customerDetails);

        verify(orderStore).placeOrder(any(Orders.class), any(Product.class), argThat(customer ->
                customer.getFirstName().equals("Alice") &&
                customer.getLastName().equals("Wonder") &&
                customer.getEmail().equals("alice@example.com") &&
                customer.getPhone().equals("+233111222333")
        ));
    }

    @Test
    @DisplayName("Should handle single quantity purchase")
    void shouldHandleSingleQuantityPurchase() {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OrderRequest orderRequest = new OrderRequest(
                productId, 1, "Ghana", "Accra", "00233", "12345"
        );
        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );
        Product product = new Product(
                productId, "Laptop", "Gaming Laptop", 1500.0, 10,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );
        Customer customer = new Customer(
                customerId, "John", "Doe", "john@example.com", "+233123456789", Instant.now()
        );

        when(productStore.getProduct(productId)).thenReturn(Optional.of(product));
        when(customerStore.findByEmail("john@example.com")).thenReturn(Optional.of(customer));

        purchaseService.purchaseProduct(orderRequest, customerDetails);

        verify(orderStore).placeOrder(any(Orders.class), argThat(updatedProduct ->
                updatedProduct.getStockQuantity() == 9
        ), eq(customer));
    }

    @Test
    @DisplayName("Should get purchase history successfully")
    void shouldGetPurchaseSuccessfully() {
        UUID customerId1 = UUID.randomUUID();
        UUID customerId2 = UUID.randomUUID();
        int limit = 10;
        int offset = 0;

        Orders order1 = new Orders(
                UUID.randomUUID(), customerId1, Instant.now(), 1500.0,
                "Ghana", "Accra", "00233"
        );
        Orders order2 = new Orders(
                UUID.randomUUID(), customerId2, Instant.now(), 800.0,
                "Ghana", "Kumasi", "00234"
        );
        List<Orders> orders = List.of(order1, order2);

        Customer customer1 = new Customer(
                customerId1, "John", "Doe", "john@example.com", "+233123456789", Instant.now()
        );
        Customer customer2 = new Customer(
                customerId2, "Jane", "Smith", "jane@example.com", "+233987654321", Instant.now()
        );
        List<Customer> customers = List.of(customer1, customer2);

        when(orderStore.getAllOrders(limit, offset)).thenReturn(orders);
        when(customerStore.findByMultipleIds(any())).thenReturn(customers);

        List<OrderResponse> result = purchaseService.getPurchaseHistory(limit, offset);

        Assertions.assertEquals(2, result.size());
        verify(orderStore).getAllOrders(limit, offset);
        verify(customerStore).findByMultipleIds(argThat(ids ->
                ids.contains(customerId1) && ids.contains(customerId2)
        ));
    }

    @Test
    @DisplayName("Should get purchase history with pagination")
    void shouldGetPurchaseHistoryWithPagination() {
        int limit = 5;
        int offset = 10;

        when(orderStore.getAllOrders(limit, offset)).thenReturn(List.of());
        when(customerStore.findByMultipleIds(any())).thenReturn(List.of());

        purchaseService.getPurchaseHistory(limit, offset);

        verify(orderStore).getAllOrders(limit, offset);
    }

    @Test
    @DisplayName("Should batch fetch customers only once for purchase history")
    void shouldBatchFetchCustomersOnlyOnceForPurchaseHistory() {
        UUID customerId = UUID.randomUUID();
        int limit = 10;
        int offset = 0;

        Orders order1 = new Orders(
                UUID.randomUUID(), customerId, Instant.now(), 1500.0,
                "Ghana", "Accra", "00233"
        );
        Orders order2 = new Orders(
                UUID.randomUUID(), customerId, Instant.now(), 800.0,
                "Ghana", "Kumasi", "00234"
        );
        List<Orders> orders = List.of(order1, order2);

        Customer customer = new Customer(
                customerId, "John", "Doe", "john@example.com", "+233123456789", Instant.now()
        );

        when(orderStore.getAllOrders(limit, offset)).thenReturn(orders);
        when(customerStore.findByMultipleIds(any())).thenReturn(List.of(customer));

        purchaseService.getPurchaseHistory(limit, offset);

        verify(customerStore, times(1)).findByMultipleIds(any());
    }

    @Test
    @DisplayName("Should count zero purchases when no orders exist")
    void shouldCountZeroPurchasesWhenNoOrdersExist() {
        when(orderStore.countAll()).thenReturn(0);

        int result = purchaseService.countPurchases();

        Assertions.assertEquals(0, result);
        verify(orderStore).countAll();
    }

}
