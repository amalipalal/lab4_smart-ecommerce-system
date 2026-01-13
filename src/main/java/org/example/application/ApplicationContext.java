package org.example.application;

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
import org.example.service.ProductService;

public class ApplicationContext {

    private static ApplicationContext instance;
    private final CategoryService categoryService;
    private final ProductService productService;

    private ApplicationContext() {
        CategoryReadDao categoryReadDao = new SqlCategoryReadDao();
        ProductReadDao productReadDao = new SqlProductReadDao();

        var cache = new ProductCache();

        CategoryWriteDaoFactory categoryWriteFactory = new SqlCategoryWriteDaoFactory();
        ProductWriteDaoFactory productWriteDaoFactory = new SqlProductWriteDaoFactory();

        this.categoryService = new CategoryService(categoryReadDao,
                new SqlUnitOfWorkFactory(), categoryWriteFactory, cache);
        this.productService = new ProductService(productReadDao,
                new ProductCache(), productWriteDaoFactory, new SqlUnitOfWorkFactory());
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
}