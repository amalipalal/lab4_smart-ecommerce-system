package org.example.application;

import org.example.config.DataSource;
import org.example.config.DatabaseConfig;
import org.example.dao.impl.*;
import org.example.dao.interfaces.*;
import org.example.service.CategoryService;
import org.example.service.PurchaseService;
import org.example.service.ProductService;
import org.example.service.ReviewService;
import org.example.store.category.CategoryStore;
import org.example.store.customer.CustomerStore;
import org.example.store.order.OrderStore;
import org.example.store.product.ProductStore;
import org.example.store.review.ReviewStore;

public class ApplicationContext {

    private static ApplicationContext instance;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final PurchaseService purchaseService;
    private final ReviewService reviewService;

    private ApplicationContext() {
        DataSource dataSource = new DataSource(DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER, DatabaseConfig.DB_PASSWORD);

        var cache = new ApplicationCache();

        CustomerDao customerDao = new CustomerJdbcDao();
        ProductDao productDao = new ProductJdbcDao();
        OrdersDao ordersDao = new OrderJdbcDao();
        CategoryDao categoryDao = new CategoryJdbcDao();
        ReviewDAO reviewDAO = new ReviewJdbcDAO();

        OrderStore orderStore = new OrderStore(dataSource,
                cache, customerDao, productDao, ordersDao);
        ProductStore productStore = new ProductStore(dataSource, cache, productDao);
        CategoryStore categoryStore = new CategoryStore(dataSource, cache, categoryDao);
        CustomerStore customerStore = new CustomerStore(dataSource, cache, customerDao);
        ReviewStore reviewStore = new ReviewStore(dataSource, cache, reviewDAO);

        this.categoryService = new CategoryService(categoryStore);
        this.productService = new ProductService(productStore);
        this.purchaseService = new PurchaseService(orderStore, productStore, customerStore);
        this.reviewService = new ReviewService(reviewStore,customerStore);
    }

    public static ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }

    public ProductService getProductService() {
        return productService;
    }

    public PurchaseService getPurchaseService() { return purchaseService; }

    public ReviewService getReviewService() {return reviewService;}
}