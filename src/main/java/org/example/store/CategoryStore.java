package org.example.store;

import org.example.cache.ProductCache;
import org.example.config.DataSource;
import org.example.dao.interfaces.CategoryDao;
import org.example.model.Category;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryStore {
    private final DataSource dataSource;
    private final ProductCache cache;
    private final CategoryDao categoryDao;

    public CategoryStore(DataSource dataSource, ProductCache cache, CategoryDao categoryDao) {
        this.dataSource = dataSource;
        this.cache = cache;
        this.categoryDao = categoryDao;
    }

    public Category createCategory(Category category) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                categoryDao.save(conn, category);
                conn.commit();
                cache.invalidateByPrefix("category:");
                return category;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public Category updateCategory(Category category) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                categoryDao.update(conn, category);
                conn.commit();
                cache.invalidateByPrefix("category:");
                return category;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public Optional<Category> getCategory(UUID id) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:" + id;
            return cache.getOrLoad(key, () -> categoryDao.findById(conn, id));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public Optional<Category> getCategoryByName(String name) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:name:" + name;
            return cache.getOrLoad(key, () -> categoryDao.findByName(conn, name));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Category> searchByName(String query, int limit, int offset) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:search:" + query + ":" + limit + ":" + offset;
            return cache.getOrLoad(key, () -> categoryDao.searchByName(conn, query, limit, offset));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public List<Category> findAll(int limit, int offset) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:all:" + limit + ":" + offset;
            return cache.getOrLoad(key, () -> categoryDao.findAll(conn, limit, offset));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public int count() {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:count";
            return cache.getOrLoad(key, () -> categoryDao.count(conn));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public int countByName(String query) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:count:" + query;
            return cache.getOrLoad(key, () -> categoryDao.countByName(conn, query));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }
}
