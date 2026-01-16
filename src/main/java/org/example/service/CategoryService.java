package org.example.service;

import org.example.dto.category.CreateCategoryRequest;
import org.example.dto.category.CategoryResponse;
import org.example.dto.category.UpdateCategoryRequest;
import org.example.model.Category;
import org.example.store.CategoryStore;

import java.util.List;
import java.util.UUID;

public class CategoryService {

    private final CategoryStore categoryStore;

    public CategoryService(CategoryStore categoryStore) {
        this.categoryStore = categoryStore;
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = categoryStore.createCategory(request);
        return new CategoryResponse(category);
    }

    public CategoryResponse updateCategory(UpdateCategoryRequest request) {
        Category updated = categoryStore.updateCategory(request);
        return new CategoryResponse(updated);
    }

    public CategoryResponse getCategory(UUID id) {
        Category category = categoryStore.getCategory(id);
        return new CategoryResponse(category);
    }

    public CategoryResponse getCategory(String name) {
        Category category = categoryStore.getCategoryByName(name);
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
