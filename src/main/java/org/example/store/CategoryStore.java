package org.example.store;

import org.example.cache.ProductCache;
import org.example.config.DataSource;
import org.example.dao.interfaces.CategoryDao;
import org.example.dto.category.CreateCategoryRequest;
import org.example.dto.category.UpdateCategoryRequest;
import org.example.model.Category;
import org.example.service.exception.CategoryNotFoundException;
import org.example.service.exception.DuplicateCategoryException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
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

    public Category createCategory(CreateCategoryRequest request) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Category> existing = categoryDao.findByName(conn, request.name());
                if (existing.isPresent()) throw new DuplicateCategoryException(request.name());

                Category category = new Category(
                        UUID.randomUUID(),
                        request.name(),
                        request.description(),
                        Instant.now(),
                        Instant.now()
                );

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

    public Category updateCategory(UpdateCategoryRequest request) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Category existing = categoryDao.findById(conn, request.categoryId())
                        .orElseThrow(() -> new CategoryNotFoundException(request.categoryId().toString()));

                if (!existing.getName().equals(request.name()) &&
                        categoryDao.findByName(conn, request.name()).isPresent()) {
                    throw new DuplicateCategoryException(request.name());
                }

                Category updated = new Category(
                        existing.getCategoryId(),
                        request.name(),
                        request.description(),
                        existing.getCreatedAt(),
                        Instant.now()
                );

                categoryDao.update(conn, updated);
                conn.commit();
                cache.invalidateByPrefix("category:");
                return updated;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public Category getCategory(UUID id) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:" + id;
            return cache.getOrLoad(key, () -> categoryDao.findById(conn, id)
                    .orElseThrow(() -> new CategoryNotFoundException(id.toString())));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public Category getCategoryByName(String name) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:name:" + name;
            return cache.getOrLoad(key, () -> categoryDao.findByName(conn, name)
                    .orElseThrow(() -> new CategoryNotFoundException(name)));
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
