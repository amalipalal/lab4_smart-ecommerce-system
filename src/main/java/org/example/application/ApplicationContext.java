package org.example.application;

import org.example.config.DataSource;
import org.example.config.DatabaseConfig;
import org.example.dao.impl.customer.CustomerJdbcDao;
import org.example.dao.impl.customer.SqlCustomerReadDao;
import org.example.dao.impl.customer.SqlCustomerWriteDaoFactory;
import org.example.dao.impl.order.OrderJdbcDao;
import org.example.dao.impl.order.SqlOrderWriteDaoFactory;
import org.example.dao.impl.product.ProductJdbcDao;
import org.example.dao.interfaces.customer.CustomerDao;
import org.example.dao.interfaces.customer.CustomerReadDao;
import org.example.dao.interfaces.customer.CustomerWriteDaoFactory;
import org.example.dao.interfaces.order.OrderWriteDaoFactory;
import org.example.dao.interfaces.order.OrdersDao;
import org.example.dao.interfaces.product.ProductDao;
import org.example.persistence.impl.sql.SqlUnitOfWorkFactory;
import org.example.cache.ProductCache;
import org.example.dao.impl.SqlCategoryWriteDaoFactory;
import org.example.dao.impl.SqlProductWriteDaoFactory;
import org.example.dao.impl.category.SqlCategoryReadDao;
import org.example.dao.impl.product.SqlProductReadDao;
import org.example.dao.interfaces.CategoryWriteDaoFactory;
import org.example.dao.interfaces.ProductWriteDaoFactory;
import org.example.dao.interfaces.category.CategoryReadDao;
import org.example.dao.interfaces.product.ProductReadDao;
import org.example.service.CategoryService;
import org.example.service.PurchaseService;
import org.example.service.ProductService;
import org.example.store.OrderStore;

public class ApplicationContext {

    private static ApplicationContext instance;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final PurchaseService purchaseService;

    private ApplicationContext() {
        DataSource dataSource = new DataSource(
                DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
        );


        var cache = new ProductCache();

        CustomerDao customerDao = new CustomerJdbcDao();
        ProductDao productDao = new ProductJdbcDao();
        OrdersDao ordersDao = new OrderJdbcDao();

        OrderStore orderStore = new OrderStore(
                dataSource,
                cache,
                customerDao,
                productDao,
                ordersDao
        );

        CategoryReadDao categoryReadDao = new SqlCategoryReadDao();
        ProductReadDao productReadDao = new SqlProductReadDao();
        CustomerReadDao customerReadDao = new SqlCustomerReadDao();

        CategoryWriteDaoFactory categoryWriteFactory = new SqlCategoryWriteDaoFactory();
        ProductWriteDaoFactory productWriteDaoFactory = new SqlProductWriteDaoFactory();
        OrderWriteDaoFactory orderWriteDaoFactory = new SqlOrderWriteDaoFactory();
        CustomerWriteDaoFactory customerWriteDaoFactory = new SqlCustomerWriteDaoFactory();

        this.categoryService = new CategoryService(categoryReadDao,
                new SqlUnitOfWorkFactory(), categoryWriteFactory, cache);
        this.productService = new ProductService(productReadDao,
                cache, productWriteDaoFactory, new SqlUnitOfWorkFactory());
        this.purchaseService = new PurchaseService(orderStore);
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