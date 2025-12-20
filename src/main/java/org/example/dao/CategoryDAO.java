package org.example.dao;

import org.example.dao.exception.DAOException;
import org.example.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryDAO {

    Optional<Category> findById(UUID categoryId) throws DAOException;

    Optional<Category> findByName(String name) throws DAOException;

    List<Category> findAll(int limit, int offset) throws DAOException;

    void save(Category category) throws DAOException;

    void update(Category category) throws DAOException;

}
