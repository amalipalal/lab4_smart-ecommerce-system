package org.example;

import org.example.cache.ProductCache;
import org.example.dao.impl.category.SqlCategoryReadDao;
import org.example.dao.impl.product.SqlProductReadDao;
import org.example.dao.interfaces.CategoryDAO;
import org.example.dao.impl.CategoryJdbcDAO;
import org.example.dao.interfaces.category.CategoryReadDao;
import org.example.dao.interfaces.product.ProductReadDao;
import org.example.service.CategoryService;
import org.example.service.ProductService;

public class ApplicationContext {

    private static ApplicationContext instance;

    private final CategoryService categoryService;
    private final ProductService productService;

    private ApplicationContext() {
        CategoryDAO categoryDOA = new CategoryJdbcDAO();
        CategoryReadDao categoryReadDao = new SqlCategoryReadDao();
        ProductReadDao productReadDao = new SqlProductReadDao();

        var cache = new ProductCache();

        this.categoryService = new CategoryService(categoryReadDao, new SqlUnitOfWorkFactory(), cache);
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