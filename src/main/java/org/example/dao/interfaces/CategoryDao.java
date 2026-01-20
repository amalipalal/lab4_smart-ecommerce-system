package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Category;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryDao {

    /**
     * Find a category by id.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param categoryId category identifier
     * @return optional category when found
     * @throws DAOException on DAO errors
     */
    Optional<Category> findById(Connection connection, UUID categoryId) throws DAOException;

    /**
     * Find a category by name.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param name category name
     * @return optional category when found
     * @throws DAOException on DAO errors
     */
    Optional<Category> findByName(Connection connection, String name) throws DAOException;

    /**
     * Search categories by name with paging.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param query substring to search for
     * @param limit maximum results
     * @param offset zero-based offset
     * @return list of matching categories
     * @throws DAOException on DAO errors
     */
    List<Category> searchByName(Connection connection, String query, int limit, int offset) throws DAOException;

    /**
     * Count categories matching a name query.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param query name substring
     * @return number of matching categories
     * @throws DAOException on DAO errors
     */
    int countByName(Connection connection, String query) throws DAOException;

    /**
     * Retrieve all categories with paging.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param limit maximum results
     * @param offset zero-based offset
     * @return list of categories
     * @throws DAOException on DAO errors
     */
    List<Category> findAll(Connection connection, int limit, int offset) throws DAOException;

    /**
     * Persist a new {@link Category}.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param category category to save
     * @throws DAOException on DAO errors
     */
    void save(Connection connection, Category category) throws DAOException;

    /**
     * Update an existing {@link Category}.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param category category to update
     * @throws DAOException on DAO errors
     */
    void update(Connection connection, Category category) throws DAOException;

    /**
     * Count all categories.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @return total number of categories
     * @throws DAOException on DAO errors
     */
    int count(Connection connection) throws DAOException;

}
