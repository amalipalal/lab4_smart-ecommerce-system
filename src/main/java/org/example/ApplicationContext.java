package org.example;

import org.example.cache.ProductCache;
import org.example.dao.impl.product.SqlProductReadDao;
import org.example.dao.interfaces.CategoryDAO;
import org.example.dao.impl.CategoryJdbcDAO;
import org.example.dao.interfaces.product.ProductReadDao;
import org.example.service.CategoryService;
import org.example.service.ProductService;

public class ApplicationContext {

    private static ApplicationContext instance;

    private final CategoryService categoryService;
    private final ProductService productService;

    private ApplicationContext() {
        CategoryDAO categoryDOA = new CategoryJdbcDAO();
        ProductReadDao productReadDao = new SqlProductReadDao();

        this.categoryService = new CategoryService(categoryDOA);
        this.productService = new ProductService(productReadDao, new ProductCache(), new SqlUnitOfWorkFactory());
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