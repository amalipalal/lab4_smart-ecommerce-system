package org.example.service;

import org.example.dto.category.CreateCategoryRequest;
import org.example.dto.category.CategoryResponse;
import org.example.dto.category.UpdateCategoryRequest;
import org.example.model.Category;
import org.example.store.category.CategoryStore;
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

    /**
     * Create a new category if one with the same name does not already exist.
     *
     * Uses {@link CategoryStore#getCategoryByName(String)} to check duplicates and
     * {@link CategoryStore#createCategory(org.example.model.Category)} to persist.
     *
     * @param request the incoming {@link CreateCategoryRequest}
     * @return a {@link CategoryResponse} for the newly created category
     * @throws DuplicateCategoryException if a category with the same name already exists
     */
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

    /**
     * Update an existing category.
     *
     * Validates existence via {@link CategoryStore#getCategory(java.util.UUID)} and checks for duplicate
     * names using {@link CategoryStore#getCategoryByName(String)} before delegating to
     * {@link CategoryStore#updateCategory(org.example.model.Category)}.
     *
     * @param request the {@link UpdateCategoryRequest} containing updated fields
     * @return a {@link CategoryResponse} for the updated category
     * @throws CategoryNotFoundException if the category to update does not exist
     * @throws DuplicateCategoryException if another category with the same name exists
     */
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

    /**
     * Retrieve a category by id.
     *
     * Delegates to {@link CategoryStore#getCategory(java.util.UUID)} and throws {@link CategoryNotFoundException}
     * if not present.
     *
     * @param id the category id
     * @return a {@link CategoryResponse}
     * @throws CategoryNotFoundException if the category does not exist
     */
    public CategoryResponse getCategory(UUID id) {
        Category category = categoryStore.getCategory(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        return new CategoryResponse(category);
    }

    /**
     * Retrieve a category by name.
     *
     * Delegates to {@link CategoryStore#getCategoryByName(String)} and throws {@link CategoryNotFoundException}
     * if not present.
     *
     * @param name the category name
     * @return a {@link CategoryResponse}
     * @throws CategoryNotFoundException if the category does not exist
     */
    public CategoryResponse getCategory(String name) {
        Category category = categoryStore.getCategoryByName(name)
                .orElseThrow(() -> new CategoryNotFoundException(name));
        return new CategoryResponse(category);
    }

    /**
     * Search categories by name with paging.
     *
     * @param query  substring to search for
     * @param limit  maximum results
     * @param offset zero-based paging offset
     * @return list of {@link CategoryResponse}
     */
    public List<CategoryResponse> getCategory(String query, int limit, int offset) {
        List<Category> categories = categoryStore.searchByName(query, limit, offset);
        return categories.stream().map(CategoryResponse::new).toList();
    }

    /**
     * Retrieve all categories with paging.
     *
     * @param limit  maximum results
     * @param offset zero-based offset
     * @return list of {@link CategoryResponse}
     */
    public List<CategoryResponse> getAllCategories(int limit, int offset) {
        List<Category> categories = categoryStore.findAll(limit, offset);
        return categories.stream().map(CategoryResponse::new).toList();
    }

    public int getCategoryCount() {
        return categoryStore.count();
    }

    /**
     * Search categories by name with paging.
     *
     * Delegates to {@link CategoryStore#searchByName(String, int, int)}.
     *
     * @param query  substring to search for
     * @param limit  maximum results
     * @param offset zero-based offset
     * @return list of {@link CategoryResponse}
     */
    public List<CategoryResponse> searchCategories(String query, int limit, int offset) {
        List<Category> categories = categoryStore.searchByName(query, limit, offset);
        return categories.stream().map(CategoryResponse::new).toList();
    }

    /**
     * Count categories that match a name query.
     *
     * @param query name substring to count
     * @return number of matching categories
     */
    public int countCategoriesByName(String query) {
        return categoryStore.countByName(query);
    }
}
