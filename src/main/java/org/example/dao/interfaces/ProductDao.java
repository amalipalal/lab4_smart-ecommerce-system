package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Product;
import org.example.model.ProductFilter;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductDao {

    Optional<Product> findById(Connection connection, UUID productId) throws DAOException;

    List<Product> findAll(Connection connection, int limit, int offset) throws DAOException;

    int countAll(Connection connection) throws DAOException;

    List<Product> findFiltered(Connection connection, ProductFilter filter, int limit, int offset) throws DAOException;

    int countFiltered(Connection connection, ProductFilter filter) throws DAOException;

    void save(Connection connection, Product product) throws DAOException;

    void update(Connection connection, Product product) throws DAOException;

    void reduceStock(Connection connection, UUID productId, int quantity) throws DAOException;

    void increaseStock(Connection connection, UUID productId, int quantity) throws DAOException;

    void deleteById(Connection connection, UUID productId) throws DAOException;
}
