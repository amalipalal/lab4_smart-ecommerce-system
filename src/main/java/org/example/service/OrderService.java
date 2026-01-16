package org.example.service;

import org.example.cache.ProductCache;
import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.OrdersDAO;
import org.example.dao.interfaces.ProductWriteDaoFactory;
import org.example.dao.interfaces.customer.CustomerReadDao;
import org.example.dao.interfaces.customer.CustomerWriteDao;
import org.example.dao.interfaces.customer.CustomerWriteDaoFactory;
import org.example.dao.interfaces.order.OrderWriteDao;
import org.example.dao.interfaces.order.OrderWriteDaoFactory;
import org.example.dao.interfaces.product.ProductDAO;
import org.example.dao.interfaces.product.ProductReadDao;
import org.example.dao.interfaces.product.ProductWriteDao;
import org.example.dto.order.CustomerDetails;
import org.example.dto.order.OrderRequest;
import org.example.model.Customer;
import org.example.model.Orders;
import org.example.model.Product;
import org.example.persistence.UnitOfWork;
import org.example.persistence.UnitOfWorkFactory;
import org.example.service.exception.InsufficientProductStock;
import org.example.service.exception.ProductNotFoundException;
import org.example.util.data.DBConnection;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class OrderService {
    private final CustomerReadDao customerReadDao;
    private final ProductReadDao productReadDao;
    private final UnitOfWorkFactory unitOfWorkFactory;
    private final CustomerWriteDaoFactory customerWriteDaoFactory;
    private final ProductWriteDaoFactory productWriteDaoFactory;
    private final OrderWriteDaoFactory orderWriteDaoFactory;
    private final ProductCache cache;

    public OrderService(
            CustomerReadDao customerReadDao,
            ProductReadDao productReadDao,
            UnitOfWorkFactory unitOfWorkFactory,
            CustomerWriteDaoFactory customerWriteDaoFactory,
            ProductWriteDaoFactory productWriteDaoFactory,
            OrderWriteDaoFactory orderWriteDaoFactory,
            ProductCache cache
    ) {
        this.customerReadDao = customerReadDao;
        this.productReadDao = productReadDao;
        this.unitOfWorkFactory = unitOfWorkFactory;
        this.customerWriteDaoFactory = customerWriteDaoFactory;
        this.productWriteDaoFactory = productWriteDaoFactory;
        this.orderWriteDaoFactory = orderWriteDaoFactory;
        this.cache = cache;
    }

    public void placeOrder(OrderRequest orderRequest, CustomerDetails customerDetails) {
        // create a unit of work object to represent a single transaction
        UnitOfWork unitOfWork = unitOfWorkFactory.create();
        try {
            Product product = retrieveProduct(orderRequest);
            if(product.getStockQuantity() - orderRequest.quantity() < 0)
                throw new InsufficientProductStock(product.getProductId().toString());

            ProductWriteDao productWriteDao = this.productWriteDaoFactory.create(unitOfWork.getConnection());
            productWriteDao.reduceStock(product.getProductId(), orderRequest.quantity());

            Customer customer = createOrFindCustomer(customerDetails, unitOfWork);
            storeProduct(orderRequest, unitOfWork, product, customer.getCustomerId());

            unitOfWork.commit();
            invalidateCache(product.getProductId());
        } catch (Exception e) {
            unitOfWork.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            unitOfWork.close();
        }
    }

    private Product retrieveProduct(OrderRequest orderRequest) {
        UUID productId = orderRequest.productId();
        String key = "product:" + productId.toString();
        return this.cache.getOrLoad(key, () -> this.productReadDao.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString())));
    }

    private Customer createOrFindCustomer(CustomerDetails customerDetails, UnitOfWork unitOfWork) {
        String key = "customer:" + customerDetails.email();
        Optional<Customer> customer = cache.getOrLoad(key, () -> customerReadDao.findByEmail(customerDetails.email()));

        if (customer.isPresent())
            return customer.get();
        CustomerWriteDao writeDao = customerWriteDaoFactory.create(unitOfWork.getConnection());
        Customer newCustomer = new Customer(
                UUID.randomUUID(),
                customerDetails.firstName(),
                customerDetails.lastName(),
                customerDetails.email(),
                customerDetails.phone(),
                Instant.now()
        );
        writeDao.save(newCustomer);
        return newCustomer;
    }

    private void storeProduct(OrderRequest orderRequest, UnitOfWork unitOfWork, Product product, UUID customerId) {
        double totalPrice = product.getPrice() * orderRequest.quantity();
        Orders order = createOrder(orderRequest, customerId, totalPrice);

        OrderWriteDao orderWriteDao = this.orderWriteDaoFactory.create(unitOfWork.getConnection());
        orderWriteDao.save(order);
    }

    private Orders createOrder(OrderRequest orderRequest, UUID customerId, double totalPrice) {
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

    private void invalidateCache(UUID productId) {
        cache.invalidate("product:" + productId);
        cache.invalidateByPrefix("product:all:");
        cache.invalidateByPrefix("product:search:");
        cache.invalidateByPrefix("product:count:");
    }
}
