package org.example.service;

import org.example.dao.interfaces.ProductDAO;
import org.example.dao.exception.DAOException;

import java.util.UUID;

public class InventoryService {

    private final ProductDAO productDAO;

    public InventoryService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public void reduceStock(UUID productId, int quantity) {
        if(quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");

        try {
            this.productDAO.reduceStock(productId, quantity);
        } catch (DAOException e) {
            throw new RuntimeException("Inventory update failed", e);
        }
    }

    public void increaseStock(UUID productId, int quantity) {
        if(quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");

        try {
            this.productDAO.increaseStock(productId, quantity);
        } catch (DAOException e) {
            throw new RuntimeException("Inventory increase failed", e);
        }
    }
}
