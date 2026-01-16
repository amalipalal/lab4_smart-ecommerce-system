package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Category;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryDao {

    Optional<Category> findById(Connection connection, UUID categoryId) throws DAOException;

    Optional<Category> findByName(Connection connection, String name) throws DAOException;

    List<Category> searchByName(Connection connection, String query, int limit, int offset) throws DAOException;

    int countByName(Connection connection, String query) throws DAOException;

    List<Category> findAll(Connection connection, int limit, int offset) throws DAOException;

    void save(Connection connection, Category category) throws DAOException;

    void update(Connection connection, Category category) throws DAOException;

    int count(Connection connection) throws DAOException;

}
