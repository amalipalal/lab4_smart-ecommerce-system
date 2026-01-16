package org.example.store;

import org.example.cache.ProductCache;
import org.example.config.DataSource;
import org.example.dao.interfaces.product.ProductDao;
import org.example.dto.product.CreateProductRequest;
import org.example.dto.product.UpdateProductRequest;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.service.exception.ProductNotFoundException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
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

    public Product createProduct(CreateProductRequest request) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Product product = new Product(
                        UUID.randomUUID(),
                        request.name(),
                        request.description(),
                        request.price(),
                        request.stock(),
                        request.categoryId(),
                        Instant.now(),
                        Instant.now()
                );
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

    public Product updateProduct(UUID productId, UpdateProductRequest request) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Product existing = this.productDao.findById(conn, productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
                if (request.name() != null) existing.setName(request.name());
                if (request.description() != null) existing.setDescription(request.description());
                if (request.price() != null) existing.setPrice(request.price());
                if (request.stock() != null) existing.setStockQuantity(request.stock());
                if (request.categoryId() != null) existing.setCategoryId(request.categoryId());
                existing.setUpdatedAt(Instant.now());
                this.productDao.update(conn, existing);
                conn.commit();
                invalidateAllProductCache();
                return existing;
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

    public Product getProduct(UUID productId) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "product:" + productId.toString();
            return this.cache.getOrLoad(key, () -> this.productDao.findById(conn, productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId.toString())));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Product> getAllProducts(int limit, int offset) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "product:all:" + limit + ":" + offset;
            return this.cache.getOrLoad(key, () -> this.productDao.findAll(conn, limit, offset));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public int countAllProducts() {
        try (Connection conn = dataSource.getConnection()) {
            return this.productDao.countAll(conn);
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

    public void reduceStock(UUID productId, int quantity) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                this.productDao.reduceStock(conn, productId, quantity);
                conn.commit();
                invalidateAllProductCache();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to reduce stock.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public void increaseStock(UUID productId, int quantity) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                this.productDao.increaseStock(conn, productId, quantity);
                conn.commit();
                invalidateAllProductCache();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to increase stock.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    private void invalidateAllProductCache() {
        this.cache.invalidateByPrefix("product:");
    }
}
