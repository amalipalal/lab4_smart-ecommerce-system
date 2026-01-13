package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Product;

import java.util.UUID;

public interface ProductWriteDao {

    void save(Product product) throws DAOException;

    void update(Product product) throws DAOException;

    void reduceStock(UUID productId, int quantity) throws DAOException;

    void increaseStock(UUID productId, int quantity) throws DAOException;

    void deleteById(UUID productId) throws DAOException;
}
