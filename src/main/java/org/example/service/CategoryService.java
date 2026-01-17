package org.example.service;

import org.example.dto.category.CreateCategoryRequest;
import org.example.dto.category.CategoryResponse;
import org.example.dto.category.UpdateCategoryRequest;
import org.example.model.Category;
import org.example.store.CategoryStore;
import org.example.service.exception.CategoryNotFoundException;
import org.example.service.exception.DuplicateCategoryException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryService {

    private final CategoryStore categoryStore;

    public CategoryService(CategoryStore categoryStore) {
        this.categoryStore = categoryStore;
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Optional<Category> existing = categoryStore.getCategoryByName(request.name());
        if (existing.isPresent()) throw new DuplicateCategoryException(request.name());
        Category category = new Category(
                UUID.randomUUID(),
                request.name(),
                request.description(),
                Instant.now(),
                Instant.now()
        );
        Category saved = categoryStore.createCategory(category);
        return new CategoryResponse(saved);
    }

    public CategoryResponse updateCategory(UpdateCategoryRequest request) {
        Category existingOption = categoryStore.getCategory(request.categoryId()).orElseThrow(
                () -> new CategoryNotFoundException(request.categoryId().toString()));

        boolean isDuplicate = categoryStore.getCategoryByName(request.name()).isPresent();
        if (isDuplicate) throw new DuplicateCategoryException(request.name());

        Category updated = new Category(
                existingOption.getCategoryId(),
                request.name(),
                request.description(),
                existingOption.getCreatedAt(),
                Instant.now()
        );
        Category saved = categoryStore.updateCategory(updated);
        return new CategoryResponse(saved);
    }

    public CategoryResponse getCategory(UUID id) {
        Category category = categoryStore.getCategory(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        return new CategoryResponse(category);
    }

    public CategoryResponse getCategory(String name) {
        Category category = categoryStore.getCategoryByName(name)
                .orElseThrow(() -> new CategoryNotFoundException(name));
        return new CategoryResponse(category);
    }

    public List<CategoryResponse> getCategory(String query, int limit, int offset) {
        List<Category> categories = categoryStore.searchByName(query, limit, offset);
        return categories.stream().map(CategoryResponse::new).toList();
    }

    public List<CategoryResponse> getAllCategories(int limit, int offset) {
        List<Category> categories = categoryStore.findAll(limit, offset);
        return categories.stream().map(CategoryResponse::new).toList();
    }

    public int getCategoryCount() {
        return categoryStore.count();
    }

    public List<CategoryResponse> searchCategories(String query, int limit, int offset) {
        List<Category> categories = categoryStore.searchByName(query, limit, offset);
        return categories.stream().map(CategoryResponse::new).toList();
    }

    public int countCategoriesByName(String query) {
        return categoryStore.countByName(query);
    }
}
