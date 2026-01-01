package org.example.dao;

import org.example.dao.exception.DAOException;
import org.example.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductDAO {

    Optional<Product> findById(UUID productId) throws DAOException;

    List<Product> findAll(int limit, int offset) throws DAOException;

    List<Product> findByCategory(UUID categoryId, int limit, int offset) throws DAOException;

    List<Product> searchByName(String query, int limit, int offset) throws DAOException;

    void save(Product product) throws DAOException;

    void update(Product product) throws DAOException;

    void reduceStock(UUID productId, int quantity) throws DAOException;

    void increaseStock(UUID productId, int quantity) throws DAOException;

    void deleteById(UUID productId) throws DAOException;
}
