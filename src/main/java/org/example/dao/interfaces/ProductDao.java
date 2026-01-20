package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Product;
import org.example.model.ProductFilter;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductDao {

    /**
     * Find a product by id.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param productId product identifier
     * @return optional product when found
     * @throws DAOException on DAO errors
     */
    Optional<Product> findById(Connection connection, UUID productId) throws DAOException;

    /**
     * Find all products with paging.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param limit maximum results
     * @param offset zero-based offset
     * @return list of products
     * @throws DAOException on DAO errors
     */
    List<Product> findAll(Connection connection, int limit, int offset) throws DAOException;

    /**
     * Count all products.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @return total number of products
     * @throws DAOException on DAO errors
     */
    int countAll(Connection connection) throws DAOException;

    /**
     * Find products matching a {@link ProductFilter}.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param filter filtering criteria
     * @param limit maximum results
     * @param offset zero-based offset
     * @return list of matching products
     * @throws DAOException on DAO errors
     */
    List<Product> findFiltered(Connection connection, ProductFilter filter, int limit, int offset) throws DAOException;

    /**
     * Count products matching a {@link ProductFilter}.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param filter filtering criteria
     * @return number of matching products
     * @throws DAOException on DAO errors
     */
    int countFiltered(Connection connection, ProductFilter filter) throws DAOException;

    /**
     * Persist a new {@link Product}.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param product product to save
     * @throws DAOException on DAO errors
     */
    void save(Connection connection, Product product) throws DAOException;

    /**
     * Update an existing {@link Product}.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param product product to update
     * @throws DAOException on DAO errors
     */
    void update(Connection connection, Product product) throws DAOException;

    /**
     * Reduce stock for a product.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param productId product identifier
     * @param quantity amount to reduce
     * @throws DAOException on DAO errors
     */
    void reduceStock(Connection connection, UUID productId, int quantity) throws DAOException;

    /**
     * Increase stock for a product.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param productId product identifier
     * @param quantity amount to increase
     * @throws DAOException on DAO errors
     */
    void increaseStock(Connection connection, UUID productId, int quantity) throws DAOException;

    /**
     * Delete a product by id.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param productId product identifier to delete
     * @throws DAOException on DAO errors
     */
    void deleteById(Connection connection, UUID productId) throws DAOException;
}
