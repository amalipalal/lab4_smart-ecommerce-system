package org.example.store.category;

import org.example.application.ApplicationCache;
import org.example.config.DataSource;
import org.example.config.exception.DatabaseConnectionException;
import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.CategoryDao;
import org.example.model.Category;
import org.example.store.category.exception.CategoryCreationException;
import org.example.store.category.exception.CategoryRetrievalException;
import org.example.store.category.exception.CategorySearchException;
import org.example.store.category.exception.CategoryUpdateException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryStore {
    private final DataSource dataSource;
    private final ApplicationCache cache;
    private final CategoryDao categoryDao;

    public CategoryStore(DataSource dataSource, ApplicationCache cache, CategoryDao categoryDao) {
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
            } catch (DAOException e) {
                conn.rollback();
                throw new CategoryCreationException(category.getName());
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
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
            } catch (DAOException e) {
                conn.rollback();
                throw new CategoryUpdateException(category.getCategoryId().toString());
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public Optional<Category> getCategory(UUID id) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:" + id;
            return cache.getOrLoad(key, () -> categoryDao.findById(conn, id));
        } catch (DAOException e) {
            throw new CategoryRetrievalException(id.toString());
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public Optional<Category> getCategoryByName(String name) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:name:" + name;
            return cache.getOrLoad(key, () -> categoryDao.findByName(conn, name));
        } catch (DAOException e) {
            throw new CategoryRetrievalException(name);
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public List<Category> searchByName(String query, int limit, int offset) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:search:" + query + ":" + limit + ":" + offset;
            return cache.getOrLoad(key, () -> categoryDao.searchByName(conn, query, limit, offset));
        } catch (DAOException e) {
            throw new CategorySearchException("Failed to search categories");
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public List<Category> findAll(int limit, int offset) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:all:" + limit + ":" + offset;
            return cache.getOrLoad(key, () -> categoryDao.findAll(conn, limit, offset));
        } catch (DAOException e) {
            throw new CategoryRetrievalException("all");
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public int count() {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:count";
            return cache.getOrLoad(key, () -> categoryDao.count(conn));
        } catch (DAOException e) {
            throw new CategorySearchException("Failed to count categories");
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public int countByName(String query) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "category:count:" + query;
            return cache.getOrLoad(key, () -> categoryDao.countByName(conn, query));
        } catch (DAOException e) {
            throw new CategorySearchException("Failed to count categories by name");
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }
}
