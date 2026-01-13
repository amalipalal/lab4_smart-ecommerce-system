package org.example.dao.interfaces.category;

import org.example.dao.exception.DAOException;
import org.example.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryReadDao {

    Optional<Category> findById(UUID categoryId) throws DAOException;

    Optional<Category> findByName(String name) throws DAOException;

    List<Category> searchByName(String query, int limit, int offset) throws DAOException;

    int countByName(String query) throws DAOException;

    List<Category> findAll(int limit, int offset) throws DAOException;

    int count() throws DAOException;
}
