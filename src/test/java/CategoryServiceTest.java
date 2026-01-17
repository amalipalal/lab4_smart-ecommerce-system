import org.example.dto.category.CategoryResponse;
import org.example.dto.category.CreateCategoryRequest;
import org.example.dto.category.UpdateCategoryRequest;
import org.example.model.Category;
import org.example.service.CategoryService;
import org.example.service.exception.CategoryNotFoundException;
import org.example.service.exception.DuplicateCategoryException;
import org.example.store.CategoryStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryStore categoryStore;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Should create category successfully")
    void shouldCreateCategorySuccessfully() {
        CreateCategoryRequest request = new CreateCategoryRequest("Electronics", "Electronic items");
        Category savedCategory = new Category(
                UUID.randomUUID(), "Electronics", "Electronic items",
                Instant.now(), Instant.now()
        );

        when(categoryStore.getCategoryByName("Electronics")).thenReturn(Optional.empty());
        when(categoryStore.createCategory(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse response = categoryService.createCategory(request);

        Assertions.assertEquals("Electronics", response.name());
        verify(categoryStore).getCategoryByName("Electronics");
        verify(categoryStore).createCategory(any(Category.class));
    }

    @Test
    @DisplayName("Should throw error when creating a category with duplicate name")
    void shouldThrowWhenCreatingDuplicateCategory() {
        CreateCategoryRequest request = new CreateCategoryRequest("Electronics", "Electronic items");
        Category existingCategory = new Category(
                UUID.randomUUID(), "Electronics", "Electronic items",
                Instant.now(), Instant.now()
        );

        when(categoryStore.getCategoryByName("Electronics")).thenReturn(Optional.of(existingCategory));

        Assertions.assertThrows(
                DuplicateCategoryException.class,
                () -> categoryService.createCategory(request)
        );

        verify(categoryStore).getCategoryByName("Electronics");
        verify(categoryStore, never()).createCategory(any());
    }

    @Test
    @DisplayName("Should update category successfully")
    void shouldUpdateCategorySuccessfully() {
        UUID categoryId = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest(categoryId, "Updated Name", "Updated Description");
        Category existingCategory = new Category(
                categoryId, "Old Name", "Old Description",
                Instant.now(), Instant.now()
        );
        Category updatedCategory = new Category(
                categoryId, "Updated Name", "Updated Description",
                existingCategory.getCreatedAt(), Instant.now()
        );

        when(categoryStore.getCategory(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryStore.getCategoryByName("Updated Name")).thenReturn(Optional.empty());
        when(categoryStore.updateCategory(any(Category.class))).thenReturn(updatedCategory);

        CategoryResponse response = categoryService.updateCategory(request);

        Assertions.assertEquals("Updated Name", response.name());
        Assertions.assertEquals("Updated Description", response.description());
        verify(categoryStore).getCategory(categoryId);
        verify(categoryStore).getCategoryByName("Updated Name");
        verify(categoryStore).updateCategory(any(Category.class));
    }

    @Test
    @DisplayName("Should throw error when updating non-existing category")
    void shouldThrowWhenUpdatingMissingCategory() {
        UUID id = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest(id, "new", "desc");

        when(categoryStore.getCategory(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.updateCategory(request)
        );

        verify(categoryStore).getCategory(id);
        verify(categoryStore, never()).updateCategory(any());
    }

    @Test
    @DisplayName("Should throw error when updating to duplicate name")
    void shouldThrowWhenUpdatingToDuplicateName() {
        UUID categoryId = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest(categoryId, "Existing Name", "Description");
        Category existingCategory = new Category(
                categoryId, "Old Name", "Old Description",
                Instant.now(), Instant.now()
        );
        Category duplicateCategory = new Category(
                UUID.randomUUID(), "Existing Name", "Some description",
                Instant.now(), Instant.now()
        );

        when(categoryStore.getCategory(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryStore.getCategoryByName("Existing Name")).thenReturn(Optional.of(duplicateCategory));

        Assertions.assertThrows(
                DuplicateCategoryException.class,
                () -> categoryService.updateCategory(request)
        );

        verify(categoryStore).getCategory(categoryId);
        verify(categoryStore).getCategoryByName("Existing Name");
        verify(categoryStore, never()).updateCategory(any());
    }

    @Test
    @DisplayName("Should return category when found by id")
    void shouldReturnCategoryById() {
        UUID id = UUID.randomUUID();
        Category category = new Category(
                id, "Electronics", "Electronic items", Instant.now(), Instant.now()
        );

        when(categoryStore.getCategory(id)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getCategory(id);

        Assertions.assertEquals(id, response.categoryId());
        Assertions.assertEquals("Electronics", response.name());
        verify(categoryStore).getCategory(id);
    }

    @Test
    @DisplayName("Should throw error when category not found by id")
    void shouldThrowWhenCategoryNotFoundById() {
        UUID id = UUID.randomUUID();

        when(categoryStore.getCategory(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getCategory(id)
        );

        verify(categoryStore).getCategory(id);
    }

    @Test
    @DisplayName("Should return category when found by name")
    void shouldReturnCategoryByName() {
        Category category = new Category(
                UUID.randomUUID(), "Electronics", "Electronic items",
                Instant.now(), Instant.now()
        );

        when(categoryStore.getCategoryByName("Electronics")).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getCategory("Electronics");

        Assertions.assertEquals("Electronics", response.name());
        verify(categoryStore).getCategoryByName("Electronics");
    }

    @Test
    @DisplayName("Should throw error when category not found by name")
    void shouldThrowWhenCategoryNotFoundByName() {
        when(categoryStore.getCategoryByName("missing")).thenReturn(Optional.empty());

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getCategory("missing")
        );

        verify(categoryStore).getCategoryByName("missing");
    }

    @Test
    @DisplayName("Should get all categories successfully")
    void shouldGetAllCategoriesSuccessfully() {
        List<Category> categories = List.of(
                new Category(UUID.randomUUID(), "Electronics", "Electronic items", Instant.now(), Instant.now()),
                new Category(UUID.randomUUID(), "Books", "Book items", Instant.now(), Instant.now())
        );

        when(categoryStore.findAll(10, 0)).thenReturn(categories);

        List<CategoryResponse> responses = categoryService.getAllCategories(10, 0);

        Assertions.assertEquals(2, responses.size());
        Assertions.assertEquals("Electronics", responses.get(0).name());
        Assertions.assertEquals("Books", responses.get(1).name());
        verify(categoryStore).findAll(10, 0);
    }

    @Test
    @DisplayName("Should return empty list when no categories found")
    void shouldReturnEmptyListWhenNoCategoriesFound() {
        when(categoryStore.findAll(10, 0)).thenReturn(List.of());

        List<CategoryResponse> responses = categoryService.getAllCategories(10, 0);

        Assertions.assertEquals(0, responses.size());
        verify(categoryStore).findAll(10, 0);
    }

    @Test
    @DisplayName("Should search categories by name successfully")
    void shouldSearchCategoriesByNameSuccessfully() {
        List<Category> categories = List.of(
                new Category(UUID.randomUUID(), "Electronics", "Electronic items", Instant.now(), Instant.now())
        );

        when(categoryStore.searchByName("Elec", 10, 0)).thenReturn(categories);

        List<CategoryResponse> responses = categoryService.searchCategories("Elec", 10, 0);

        Assertions.assertEquals(1, responses.size());
        Assertions.assertEquals("Electronics", responses.get(0).name());
        verify(categoryStore).searchByName("Elec", 10, 0);
    }

    @Test
    @DisplayName("Should get category count successfully")
    void shouldGetCategoryCountSuccessfully() {
        when(categoryStore.count()).thenReturn(5);

        int count = categoryService.getCategoryCount();

        Assertions.assertEquals(5, count);
        verify(categoryStore).count();
    }

    @Test
    @DisplayName("Should count categories by name successfully")
    void shouldCountCategoriesByNameSuccessfully() {
        when(categoryStore.countByName("Elec")).thenReturn(3);

        int count = categoryService.countCategoriesByName("Elec");

        Assertions.assertEquals(3, count);
        verify(categoryStore).countByName("Elec");
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        List<Category> categories = List.of(
                new Category(UUID.randomUUID(), "Category1", "Description1", Instant.now(), Instant.now()),
                new Category(UUID.randomUUID(), "Category2", "Description2", Instant.now(), Instant.now())
        );

        when(categoryStore.findAll(5, 10)).thenReturn(categories);

        List<CategoryResponse> responses = categoryService.getAllCategories(5, 10);

        Assertions.assertEquals(2, responses.size());
        verify(categoryStore).findAll(5, 10);
    }

    @Test
    @DisplayName("Should preserve created timestamp when updating category")
    void shouldPreserveCreatedTimestampWhenUpdating() {
        UUID categoryId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(86400);
        UpdateCategoryRequest request = new UpdateCategoryRequest(categoryId, "New Name", "New Description");
        Category existingCategory = new Category(
                categoryId, "Old Name", "Old Description",
                createdAt, Instant.now()
        );
        Category updatedCategory = new Category(
                categoryId, "New Name", "New Description",
                createdAt, Instant.now()
        );

        when(categoryStore.getCategory(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryStore.getCategoryByName("New Name")).thenReturn(Optional.empty());
        when(categoryStore.updateCategory(any(Category.class))).thenReturn(updatedCategory);

        CategoryResponse response = categoryService.updateCategory(request);

        Assertions.assertEquals(createdAt, response.createdAt());
        verify(categoryStore).updateCategory(argThat(category ->
                category.getCreatedAt().equals(createdAt)
        ));
    }

}
