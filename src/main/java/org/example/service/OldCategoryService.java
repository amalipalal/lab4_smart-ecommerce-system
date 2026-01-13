package org.example.service;

import org.example.dao.interfaces.CategoryDAO;
import org.example.dao.exception.DAOException;
import org.example.dto.category.CreateCategoryRequest;
import org.example.dto.category.CategoryResponse;
import org.example.dto.category.UpdateCategoryRequest;
import org.example.model.Category;
import org.example.service.exception.CategoryNotFoundException;
import org.example.service.exception.DuplicateCategoryException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OldCategoryService {

    private final CategoryDAO categoryDAO;

    public OldCategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        try {
            if (this.categoryDAO.findByName(request.name()).isPresent())
                throw new DuplicateCategoryException(request.name());

            var category = new Category(
                    UUID.randomUUID(),
                    request.name(),
                    request.description(),
                    Instant.now(),
                    Instant.now()
            );
            this.categoryDAO.save(category);

            return new CategoryResponse(category);
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public CategoryResponse updateCategory(UpdateCategoryRequest request) {
        try {
            var categoryToUpdate = this.categoryDAO.findById(request.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.categoryId().toString()));

            if(this.categoryDAO.findByName(request.name()).isPresent())
                throw new DuplicateCategoryException(request.name());

            var updatedCategory = new Category(
                    categoryToUpdate.getCategoryId(),
                    request.name(),
                    request.description(),
                    categoryToUpdate.getCreatedAt(),
                    Instant.now()
            );

            this.categoryDAO.update(updatedCategory);
            return new CategoryResponse(updatedCategory);

        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public CategoryResponse getCategory(UUID categoryId) {
        try {
            var category = this.categoryDAO.findById(categoryId)
                    .orElseThrow(() ->
                            new CategoryNotFoundException(categoryId.toString()));

            return new CategoryResponse(category);
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public CategoryResponse getCategory(String name) {
        try {
            var category = this.categoryDAO.findByName(name)
                    .orElseThrow(() ->
                            new CategoryNotFoundException(name));

            return new CategoryResponse(category);
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<CategoryResponse> getCategory(String query, int limit, int offset) {
        try {
            List<Category> categories = this.categoryDAO.searchByName(query, limit, offset);
            return categories.stream().map(CategoryResponse::new).toList();
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public int countCategoriesByName(String query) {
        try {
            return this.categoryDAO.countByName(query);
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<CategoryResponse> getAllCategories(int limit, int offset) {
        try {
            List<Category> allCategories = categoryDAO.findAll(limit, offset);
            return allCategories.stream().map(CategoryResponse::new).toList();
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public int getCategoryCount() {
        try {
            return categoryDAO.count();
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
