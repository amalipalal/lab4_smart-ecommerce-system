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

    /**
     * Persist a new {@link org.example.model.Category} within a transaction.
     *
     * Delegates to {@link org.example.dao.interfaces.CategoryDao#save(java.sql.Connection, org.example.model.Category)}
     * and invalidates category-related cache prefixes on success.
     *
     * @param category the category to create
     * @return the persisted {@link Category}
     * @throws org.example.store.category.exception.CategoryCreationException when DAO persistence fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
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

    /**
     * Update an existing {@link org.example.model.Category} inside a transaction.
     *
     * Delegates to {@link org.example.dao.interfaces.CategoryDao#update(java.sql.Connection, org.example.model.Category)}
     * and invalidates category cache on success.
     *
     * @param category the category with updated fields
     * @return the updated {@link Category}
     * @throws org.example.store.category.exception.CategoryUpdateException when DAO update fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
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

    /**
     * Load a category by id.
     *
     * Uses {@link org.example.dao.interfaces.CategoryDao#findById(java.sql.Connection, java.util.UUID)}
     * and caches the result via {@link org.example.application.ApplicationCache#getOrLoad}.
     *
     * @param id category identifier
     * @return an {@link Optional} containing the Category when found
     * @throws org.example.store.category.exception.CategoryRetrievalException when DAO retrieval fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
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

    /**
     * Load a category by name.
     *
     * Uses {@link org.example.dao.interfaces.CategoryDao#findByName(java.sql.Connection, String)}
     * and caches the result.
     *
     * @param name category name
     * @return an {@link Optional} containing the Category when found
     * @throws org.example.store.category.exception.CategoryRetrievalException when DAO retrieval fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
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

    /**
     * Search categories by name with simple paging.
     *
     * Delegates to {@link org.example.dao.interfaces.CategoryDao#searchByName(java.sql.Connection, String, int, int)}
     * and caches the result.
     *
     * @param query substring to search for
     * @param limit maximum results
     * @param offset zero-based offset
     * @return list of matching {@link Category}
     * @throws org.example.store.category.exception.CategorySearchException when DAO search fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
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

    /**
     * Retrieve a page of all categories.
     *
     * Results are loaded via {@link org.example.dao.interfaces.CategoryDao#findAll(java.sql.Connection, int, int)}
     * and cached through {@link org.example.application.ApplicationCache#getOrLoad}.
     *
     * @param limit  maximum number of categories to return
     * @param offset zero-based offset for paging
     * @return list of {@link Category} for the requested page
     * @throws org.example.store.category.exception.CategoryRetrievalException when DAO retrieval fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
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
