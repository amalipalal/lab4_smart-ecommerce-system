package org.example.store.product;

import org.example.application.ApplicationCache;
import org.example.config.DataSource;
import org.example.config.exception.DatabaseConnectionException;
import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.ProductDao;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.store.product.exception.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductStore {
    private final DataSource dataSource;
    private final ApplicationCache cache;
    private final ProductDao productDao;

    public ProductStore(DataSource dataSource, ApplicationCache cache, ProductDao productDao) {
        this.dataSource = dataSource;
        this.cache = cache;
        this.productDao = productDao;
    }

    /**
     * Persist a new {@link org.example.model.Product} inside a transaction.
     *
     * Delegates to {@link org.example.dao.interfaces.ProductDao#save(java.sql.Connection, org.example.model.Product)}
     * and invalidates product caches on success.
     *
     * @param product the product to create
     * @return the persisted {@link Product}
     * @throws org.example.store.product.exception.ProductCreationException when DAO save fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
    public Product createProduct(Product product) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                this.productDao.save(conn, product);
                conn.commit();
                invalidateAllProductCache();
                return product;
            } catch (DAOException e) {
                conn.rollback();
                throw new ProductCreationException(product.getName());
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    /**
     * Update an existing {@link org.example.model.Product} inside a transaction.
     *
     * Delegates to {@link org.example.dao.interfaces.ProductDao#update(java.sql.Connection, org.example.model.Product)}
     * and invalidates product caches on success.
     *
     * @param product product with updated fields
     * @return the updated {@link Product}
     * @throws org.example.store.product.exception.ProductUpdateException when DAO update fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
    public Product updateProduct(Product product) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                this.productDao.update(conn, product);
                conn.commit();
                invalidateAllProductCache();
                return product;
            } catch (DAOException e) {
                conn.rollback();
                throw new ProductUpdateException(product.getProductId().toString());
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    /**
     * Delete a product by id.
     *
     * @param productId product identifier
     * @throws org.example.store.product.exception.DeleteProductException when DAO delete fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
    public void deleteProduct(UUID productId) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                this.productDao.deleteById(conn, productId);
                conn.commit();
                invalidateAllProductCache();
            } catch (DAOException e) {
                conn.rollback();
                throw new DeleteProductException(productId.toString());
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    /**
     * Retrieve a product by id.
     *
     * Uses {@link org.example.dao.interfaces.ProductDao#findById(java.sql.Connection, java.util.UUID)}
     * and caches the result via {@link org.example.application.ApplicationCache#getOrLoad}.
     *
     * @param productId product identifier
     * @return an {@link Optional} containing the {@link Product} when found
     * @throws org.example.store.product.exception.ProductRetrievalException when DAO retrieval fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
    public Optional<Product> getProduct(UUID productId) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "product:" + productId.toString();
            return this.cache.getOrLoad(key, () -> this.productDao.findById(conn, productId));
        } catch (DAOException e) {
            throw new ProductRetrievalException(productId.toString());
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    /**
     * Search products using a {@link ProductFilter} with paging.
     *
     * Delegates to {@link org.example.dao.interfaces.ProductDao#findFiltered(java.sql.Connection, org.example.model.ProductFilter, int, int)}
     * and caches results.
     *
     * @param filter filter criteria
     * @param limit maximum number of results
     * @param offset zero-based offset
     * @return list of matching {@link Product}
     * @throws org.example.store.product.exception.ProductSearchException when DAO search fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
    public List<Product> searchProducts(ProductFilter filter, int limit, int offset) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "product:search:" + filter.hashCode() + limit + offset;
            return this.cache.getOrLoad(key, () -> this.productDao.findFiltered(conn, filter, limit, offset));
        } catch (DAOException e) {
            throw new ProductSearchException("Failed to search with filter");
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public int countProductsByFilter(ProductFilter filter) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "product:count:" + filter.hashCode();
            return this.cache.getOrLoad(key, () -> this.productDao.countFiltered(conn, filter));
        } catch (DAOException e) {
            throw new ProductSearchException("Failed to count search results with filter");
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    private void invalidateAllProductCache() {
        this.cache.invalidateByPrefix("product:");
    }
}
