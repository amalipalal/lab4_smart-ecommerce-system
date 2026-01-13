package org.example.service;

import org.example.UnitOfWorkFactory;
import org.example.cache.ProductCache; // same generic cache class
import org.example.dao.impl.category.SqlCategoryWriteDao;
import org.example.dao.interfaces.*;
import org.example.dao.interfaces.category.CategoryReadDao;
import org.example.dao.interfaces.category.CategoryWriteDao;
import org.example.dto.category.CreateCategoryRequest;
import org.example.dto.category.CategoryResponse;
import org.example.dto.category.UpdateCategoryRequest;
import org.example.model.Category;
import org.example.service.exception.CategoryNotFoundException;
import org.example.service.exception.DuplicateCategoryException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CategoryService {

    private final CategoryReadDao readDao;
    private final UnitOfWorkFactory unitOfWorkFactory;
    private final ProductCache cache;

    public CategoryService(
            CategoryReadDao readDao,
            UnitOfWorkFactory unitOfWorkFactory,
            ProductCache cache
    ) {
        this.readDao = readDao;
        this.unitOfWorkFactory = unitOfWorkFactory;
        this.cache = cache;
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        UnitOfWork unitOfWork = unitOfWorkFactory.create();
        try {
            CategoryWriteDao writeDao = new SqlCategoryWriteDao(unitOfWork.getConnection());

            if (readDao.findByName(request.name()).isPresent())
                throw new DuplicateCategoryException(request.name());

            var category = instantiateCategoryObject(request);

            writeDao.save(category);
            unitOfWork.commit();

            cache.invalidateByPrefix("category:");

            return new CategoryResponse(category);
        } catch (Exception e) {
            unitOfWork.rollback();
            throw new RuntimeException(e);
        } finally {
            unitOfWork.close();
        }
    }

    private Category instantiateCategoryObject(CreateCategoryRequest request) {
        return new Category(
                UUID.randomUUID(),
                request.name(),
                request.description(),
                Instant.now(),
                Instant.now()
        );
    }

    public CategoryResponse updateCategory(UpdateCategoryRequest request) {
        UnitOfWork unitOfWork = unitOfWorkFactory.create();
        try {
            CategoryWriteDao writeDao = new SqlCategoryWriteDao(unitOfWork.getConnection());

            Category existing = readDao.findById(request.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.categoryId().toString()));

            if (!existing.getName().equals(request.name()) &&
                    readDao.findByName(request.name()).isPresent()) {
                throw new DuplicateCategoryException(request.name());
            }

            Category updated = new Category(
                    existing.getCategoryId(),
                    request.name(),
                    request.description(),
                    existing.getCreatedAt(),
                    Instant.now()
            );

            writeDao.update(updated);
            unitOfWork.commit();

            cache.invalidateByPrefix("category:");

            return new CategoryResponse(updated);
        } catch (Exception e) {
            unitOfWork.rollback();
            throw new RuntimeException(e);
        } finally {
            unitOfWork.close();
        }
    }

    public CategoryResponse getCategory(UUID id) {
        String key = "category:" + id;

        Category category = cache.getOrLoad(key, () ->
                readDao.findById(id)
                        .orElseThrow(() -> new CategoryNotFoundException(id.toString()))
        );

        return new CategoryResponse(category);
    }

    public CategoryResponse getCategory(String name) {
        String key = "category:name:" + name;

        Category category = cache.getOrLoad(key, () ->
                readDao.findByName(name)
                        .orElseThrow(() -> new CategoryNotFoundException(name))
        );

        return new CategoryResponse(category);
    }

    public List<CategoryResponse> getCategory(String query, int limit, int offset) {
        String key = "category:search:" + query + ":" + limit + ":" + offset;
        List<Category> categories = cache.getOrLoad(key, () -> readDao.searchByName(query, limit, offset));
        return categories.stream().map(CategoryResponse::new).toList();
    }

    public List<CategoryResponse> getAllCategories(int limit, int offset) {
        String key = "category:all:" + limit + ":" + offset;

        List<Category> categories = cache.getOrLoad(key, () ->
                readDao.findAll(limit, offset)
        );

        return categories.stream().map(CategoryResponse::new).toList();
    }

    public int getCategoryCount() {
        String key = "category:count";
        return cache.getOrLoad(key, readDao::count);
    }

    public List<CategoryResponse> searchCategories(String query, int limit, int offset) {
        String key = "category:search:" + query + ":" + limit + ":" + offset;

        List<Category> categories = cache.getOrLoad(key, () ->
                readDao.searchByName(query, limit, offset)
        );

        return categories.stream().map(CategoryResponse::new).toList();
    }

    public int countCategoriesByName(String query) {
        String key = "category:count:" + query;

        return cache.getOrLoad(key, () ->
                readDao.countByName(query)
        );
    }
}
