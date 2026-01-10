package org.example;

import org.example.dao.interfaces.CategoryDAO;
import org.example.dao.interfaces.ProductDAO;
import org.example.dao.impl.CategoryJdbcDAO;
import org.example.dao.impl.ProductJdbcDAO;
import org.example.service.CategoryService;
import org.example.service.InventoryService;
import org.example.service.ProductService;

public class ApplicationContext {

    private static ApplicationContext instance;

    private final CategoryService categoryService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    private ApplicationContext() {
        CategoryDAO categoryDOA = new CategoryJdbcDAO();
        ProductDAO productDAO = new ProductJdbcDAO();

        this.categoryService = new CategoryService(categoryDOA);
        this.productService = new ProductService(productDAO);
        this.inventoryService = new InventoryService(productDAO);
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

    public InventoryService getInventoryService() {
        return inventoryService;
    }
}