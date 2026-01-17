package org.example.application;

import org.example.config.DataSource;
import org.example.config.DatabaseConfig;
import org.example.dao.impl.CategoryJdbcDao;
import org.example.dao.impl.CustomerJdbcDao;
import org.example.dao.impl.OrderJdbcDao;
import org.example.dao.impl.ProductJdbcDao;
import org.example.dao.interfaces.CategoryDao;
import org.example.dao.interfaces.CustomerDao;
import org.example.dao.interfaces.OrdersDao;
import org.example.dao.interfaces.ProductDao;
import org.example.cache.ProductCache;
import org.example.service.CategoryService;
import org.example.service.PurchaseService;
import org.example.service.ProductService;
import org.example.store.CategoryStore;
import org.example.store.CustomerStore;
import org.example.store.OrderStore;
import org.example.store.ProductStore;

public class ApplicationContext {

    private static ApplicationContext instance;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final PurchaseService purchaseService;

    private ApplicationContext() {
        DataSource dataSource = new DataSource(DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER, DatabaseConfig.DB_PASSWORD);

        var cache = new ProductCache();

        CustomerDao customerDao = new CustomerJdbcDao();
        ProductDao productDao = new ProductJdbcDao();
        OrdersDao ordersDao = new OrderJdbcDao();
        CategoryDao categoryDao = new CategoryJdbcDao();

        OrderStore orderStore = new OrderStore(dataSource,
                cache, customerDao, productDao, ordersDao);
        ProductStore productStore = new ProductStore(dataSource, cache, productDao);
        CategoryStore categoryStore = new CategoryStore(dataSource, cache, categoryDao);
        CustomerStore customerStore = new CustomerStore(dataSource, cache, customerDao);

        this.categoryService = new CategoryService(categoryStore);
        this.productService = new ProductService(productStore);
        this.purchaseService = new PurchaseService(orderStore, productStore, customerStore);
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
}