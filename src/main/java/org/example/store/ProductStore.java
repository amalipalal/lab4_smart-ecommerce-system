package org.example.store;

import org.example.cache.ProductCache;
import org.example.config.DataSource;
import org.example.dao.interfaces.ProductDao;
import org.example.model.Product;
import org.example.model.ProductFilter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductStore {
    private final DataSource dataSource;
    private final ProductCache cache;
    private final ProductDao productDao;

    public ProductStore(DataSource dataSource, ProductCache cache, ProductDao productDao) {
        this.dataSource = dataSource;
        this.cache = cache;
        this.productDao = productDao;
    }

    public Product createProduct(Product product) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                this.productDao.save(conn, product);
                conn.commit();
                invalidateAllProductCache();
                return product;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to create product.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public Product updateProduct(Product product) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                this.productDao.update(conn, product);
                conn.commit();
                invalidateAllProductCache();
                return product;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to update product.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public void deleteProduct(UUID productId) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                this.productDao.deleteById(conn, productId);
                conn.commit();
                invalidateAllProductCache();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to delete product.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public Optional<Product> getProduct(UUID productId) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "product:" + productId.toString();
            return this.cache.getOrLoad(key, () -> this.productDao.findById(conn, productId));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Product> searchProducts(ProductFilter filter, int limit, int offset) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "product:search:" + filter.hashCode() + limit + offset;
            return this.cache.getOrLoad(key, () -> this.productDao.findFiltered(conn, filter, limit, offset));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public int countProductsByFilter(ProductFilter filter) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "product:count:" + filter.hashCode();
            return this.cache.getOrLoad(key, () -> this.productDao.countFiltered(conn, filter));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    private void invalidateAllProductCache() {
        this.cache.invalidateByPrefix("product:");
    }
}
