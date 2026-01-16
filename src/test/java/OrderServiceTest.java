import org.example.cache.ProductCache;
import org.example.dao.interfaces.ProductWriteDaoFactory;
import org.example.dao.interfaces.customer.CustomerReadDao;
import org.example.dao.interfaces.customer.CustomerWriteDao;
import org.example.dao.interfaces.customer.CustomerWriteDaoFactory;
import org.example.dao.interfaces.order.OrderWriteDao;
import org.example.dao.interfaces.order.OrderWriteDaoFactory;
import org.example.dao.interfaces.product.ProductReadDao;
import org.example.dao.interfaces.product.ProductWriteDao;
import org.example.dto.order.CustomerDetails;
import org.example.dto.order.OrderRequest;
import org.example.model.Customer;
import org.example.model.Orders;
import org.example.model.Product;
import org.example.persistence.UnitOfWork;
import org.example.persistence.UnitOfWorkFactory;
import org.example.service.OrderService;
import org.example.service.exception.InsufficientProductStock;
import org.example.service.exception.ProductNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CustomerReadDao customerReadDao;

    @Mock
    private ProductReadDao productReadDao;

    @Mock
    private UnitOfWorkFactory unitOfWorkFactory;

    @Mock
    private CustomerWriteDaoFactory customerWriteDaoFactory;

    @Mock
    private ProductWriteDaoFactory productWriteDaoFactory;

    @Mock
    private OrderWriteDaoFactory orderWriteDaoFactory;

    @Mock
    private ProductCache cache;

    @Mock
    private UnitOfWork unitOfWork;

    @Mock
    private Connection connection;

    @Mock
    private CustomerWriteDao customerWriteDao;

    @Mock
    private ProductWriteDao productWriteDao;

    @Mock
    private OrderWriteDao orderWriteDao;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Should place order successfully for existing customer")
    void shouldPlaceOrderForExistingCustomer() {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        int quantity = 2;
        int stockQuantity = 10;

        OrderRequest orderRequest = new OrderRequest(
                productId, quantity, "Ghana", "Accra", "00233", "12345"
        );

        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );

        Product product = new Product(
                productId, "Laptop", "Gaming Laptop", 1500.0, stockQuantity,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        Customer customer = new Customer(
                customerId, "John", "Doe", "john@example.com", "+233123456789", Instant.now()
        );

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(productWriteDaoFactory.create(connection)).thenReturn(productWriteDao);
        when(orderWriteDaoFactory.create(connection)).thenReturn(orderWriteDao);

        when(cache.getOrLoad(eq("product:" + productId.toString()), any()))
                .thenAnswer(invocation -> {
                    var loader = invocation.getArgument(1, Supplier.class);
                    return loader.get();
                });

        when(productReadDao.findById(productId)).thenReturn(Optional.of(product));

        when(cache.getOrLoad(eq("customer:" + customerDetails.email()), any()))
                .thenReturn(Optional.of(customer));

        orderService.placeOrder(orderRequest, customerDetails);

        verify(productWriteDao).reduceStock(productId, quantity);
        verify(orderWriteDao).save(any(Orders.class));
        verify(unitOfWork).commit();
        verify(cache).invalidate("product:" + productId);
        verify(cache).invalidateByPrefix("product:all:");
        verify(cache).invalidateByPrefix("product:search:");
        verify(cache).invalidateByPrefix("product:count:");
        verify(unitOfWork).close();
    }

    @Test
    @DisplayName("Should place order successfully for new customer")
    void shouldPlaceOrderForNewCustomer() {
        UUID productId = UUID.randomUUID();
        int quantity = 1;
        int stockQuantity = 5;

        OrderRequest orderRequest = new OrderRequest(
                productId, quantity, "Ghana", "Kumasi", "00233", "54321"
        );

        CustomerDetails customerDetails = new CustomerDetails(
                "Jane", "Smith", "jane@example.com", "+233987654321"
        );

        Product product = new Product(
                productId, "Phone", "Smartphone", 800.0, stockQuantity,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(productWriteDaoFactory.create(connection)).thenReturn(productWriteDao);
        when(customerWriteDaoFactory.create(connection)).thenReturn(customerWriteDao);
        when(orderWriteDaoFactory.create(connection)).thenReturn(orderWriteDao);

        when(cache.getOrLoad(eq("product:" + productId.toString()), any()))
                .thenAnswer(invocation -> {
                    var loader = invocation.getArgument(1, Supplier.class);
                    return loader.get();
                });

        when(productReadDao.findById(productId)).thenReturn(Optional.of(product));

        when(cache.getOrLoad(eq("customer:" + customerDetails.email()), any()))
                .thenReturn(Optional.empty());

        orderService.placeOrder(orderRequest, customerDetails);

        verify(customerWriteDao).save(any(Customer.class));
        verify(productWriteDao).reduceStock(productId, quantity);
        verify(orderWriteDao).save(any(Orders.class));
        verify(unitOfWork).commit();
        verify(cache).invalidateByPrefix("product:all:");
        verify(unitOfWork).close();
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowWhenProductNotFound() {
        UUID productId = UUID.randomUUID();

        OrderRequest orderRequest = new OrderRequest(
                productId, 1, "Ghana", "Accra", "00233", "12345"
        );

        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);

        when(cache.getOrLoad(eq("product:" + productId.toString()), any()))
                .thenAnswer(invocation -> {
                    var loader = invocation.getArgument(1, Supplier.class);
                    return loader.get();
                });

        when(productReadDao.findById(productId)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> orderService.placeOrder(orderRequest, customerDetails)
        );

        verify(unitOfWork).close();
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void shouldThrowWhenInsufficientStock() {
        UUID productId = UUID.randomUUID();
        int requestedQuantity = 10;
        int availableStock = 5;

        OrderRequest orderRequest = new OrderRequest(
                productId, requestedQuantity, "Ghana", "Accra", "00233", "12345"
        );

        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );

        Product product = new Product(
                productId, "Laptop", "Gaming Laptop", 1500.0, availableStock,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);

        when(cache.getOrLoad(eq("product:" + productId.toString()), any()))
                .thenAnswer(invocation -> {
                    var loader = invocation.getArgument(1, Supplier.class);
                    return loader.get();
                });

        when(productReadDao.findById(productId)).thenReturn(Optional.of(product));

        Assertions.assertThrows(
                InsufficientProductStock.class,
                () -> orderService.placeOrder(orderRequest, customerDetails)
        );

        verify(unitOfWork).close();
    }

    @Test
    @DisplayName("Should throw exception when stock equals requested quantity")
    void shouldPlaceOrderWhenStockEqualsRequestedQuantity() {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        int quantity = 5;
        int stockQuantity = 5;

        OrderRequest orderRequest = new OrderRequest(
                productId, quantity, "Ghana", "Accra", "00233", "12345"
        );

        CustomerDetails customerDetails = new CustomerDetails(
                "John", "Doe", "john@example.com", "+233123456789"
        );

        Product product = new Product(
                productId, "Laptop", "Gaming Laptop", 1500.0, stockQuantity,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        Customer customer = new Customer(
                customerId, "John", "Doe", "john@example.com", "+233123456789", Instant.now()
        );

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(productWriteDaoFactory.create(connection)).thenReturn(productWriteDao);
        when(orderWriteDaoFactory.create(connection)).thenReturn(orderWriteDao);

        when(cache.getOrLoad(eq("product:" + productId.toString()), any()))
                .thenAnswer(invocation -> {
                    var loader = invocation.getArgument(1, Supplier.class);
                    return loader.get();
                });

        when(productReadDao.findById(productId)).thenReturn(Optional.of(product));

        when(cache.getOrLoad(eq("customer:" + customerDetails.email()), any()))
                .thenReturn(Optional.of(customer));

        orderService.placeOrder(orderRequest, customerDetails);

        verify(productWriteDao).reduceStock(productId, quantity);
        verify(orderWriteDao).save(any(Orders.class));
        verify(unitOfWork).commit();
    }

    @Test
    @DisplayName("Should throw exception when stock is zero")
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

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);

        when(cache.getOrLoad(eq("product:" + productId.toString()), any()))
                .thenAnswer(invocation -> {
                    var loader = invocation.getArgument(1, Supplier.class);
                    return loader.get();
                });

        when(productReadDao.findById(productId)).thenReturn(Optional.of(product));

        Assertions.assertThrows(
                InsufficientProductStock.class,
                () -> orderService.placeOrder(orderRequest, customerDetails)
        );
    }

    @Test
    @DisplayName("Should rollback transaction on failure")
    void shouldRollbackTransactionOnFailure() {
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

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(productWriteDaoFactory.create(connection)).thenReturn(productWriteDao);
        when(orderWriteDaoFactory.create(connection)).thenReturn(orderWriteDao);

        when(cache.getOrLoad(eq("product:" + productId.toString()), any()))
                .thenAnswer(invocation -> {
                    var loader = invocation.getArgument(1, Supplier.class);
                    return loader.get();
                });

        when(productReadDao.findById(productId)).thenReturn(Optional.of(product));

        when(cache.getOrLoad(eq("customer:" + customerDetails.email()), any()))
                .thenReturn(Optional.of(customer));

        doThrow(new RuntimeException("Database error")).when(orderWriteDao).save(any(Orders.class));

        Assertions.assertThrows(
                RuntimeException.class,
                () -> orderService.placeOrder(orderRequest, customerDetails)
        );

        verify(unitOfWork).rollback();
        verify(unitOfWork).close();
        verify(unitOfWork, never()).commit();
    }

    @Test
    @DisplayName("Should calculate total price correctly")
    void shouldCalculateTotalPriceCorrectly() {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        int quantity = 3;
        double price = 500.0;

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

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(productWriteDaoFactory.create(connection)).thenReturn(productWriteDao);
        when(orderWriteDaoFactory.create(connection)).thenReturn(orderWriteDao);

        when(cache.getOrLoad(eq("product:" + productId.toString()), any()))
                .thenAnswer(invocation -> {
                    var loader = invocation.getArgument(1, Supplier.class);
                    return loader.get();
                });

        when(productReadDao.findById(productId)).thenReturn(Optional.of(product));

        when(cache.getOrLoad(eq("customer:" + customerDetails.email()), any()))
                .thenReturn(Optional.of(customer));

        orderService.placeOrder(orderRequest, customerDetails);

        verify(orderWriteDao).save(argThat(order ->
                order.getTotalAmount() == price * quantity
        ));
    }

    @Test
    @DisplayName("Should save order with correct shipping details")
    void shouldSaveOrderWithCorrectShippingDetails() {
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

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(productWriteDaoFactory.create(connection)).thenReturn(productWriteDao);
        when(orderWriteDaoFactory.create(connection)).thenReturn(orderWriteDao);

        when(cache.getOrLoad(eq("product:" + productId.toString()), any()))
                .thenAnswer(invocation -> {
                    var loader = invocation.getArgument(1, Supplier.class);
                    return loader.get();
                });

        when(productReadDao.findById(productId)).thenReturn(Optional.of(product));

        when(cache.getOrLoad(eq("customer:" + customerDetails.email()), any()))
                .thenReturn(Optional.of(customer));

        orderService.placeOrder(orderRequest, customerDetails);

        verify(orderWriteDao).save(argThat(order ->
                order.getShippingCountry().equals("Ghana") &&
                order.getShippingCity().equals("Tamale") &&
                order.getShippingPostalCode().equals("98765")
        ));
    }
}
