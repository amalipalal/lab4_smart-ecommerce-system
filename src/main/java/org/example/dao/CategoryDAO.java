package org.example.dao;

import org.example.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryDAO {

    Optional<Category> findById(UUID categoryId);

    Optional<Category> findByName(String name);

    List<Category> findAll();

    void save(Category category);

    void update(Category category);

}
