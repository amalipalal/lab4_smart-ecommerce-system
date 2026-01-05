package org.example.service;

import org.example.dao.CategoryDAO;
import org.example.dao.exception.DAOException;
import org.example.dto.category.CreateCategoryRequest;
import org.example.dto.category.CreateCategoryResponse;
import org.example.model.Category;
import org.example.service.exception.CategoryNotFoundException;
import org.example.service.exception.DuplicateCategoryException;

import java.time.Instant;
import java.util.UUID;

public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public CreateCategoryResponse createCategory(CreateCategoryRequest request) {
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

            return new CreateCategoryResponse(
                    category.getCategoryId(), category.getName(), category.getDescription());
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public CreateCategoryResponse getCategory(UUID categoryId) {
        try {
            var category = this.categoryDAO.findById(categoryId)
                    .orElseThrow(() ->
                            new CategoryNotFoundException(categoryId.toString()));

            return new CreateCategoryResponse(category.getCategoryId(), category.getName(), category.getDescription());
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public CreateCategoryResponse getCategory(String name) {
        try {
            var category = this.categoryDAO.findByName(name)
                    .orElseThrow(() ->
                            new CategoryNotFoundException(name));

            return new CreateCategoryResponse(category.getCategoryId(), category.getName(), category.getDescription());
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<CreateCategoryResponse> getAllCategories(int limit, int offset) {
        try {
            List<Category> allCategories = categoryDAO.findAll(limit, offset);
            return allCategories.stream().map(CreateCategoryResponse::new).toList();
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
