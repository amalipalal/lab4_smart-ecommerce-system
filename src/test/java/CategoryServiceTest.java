import org.example.dao.interfaces.CategoryDAO;
import org.example.dao.exception.DAOException;
import org.example.dto.category.CreateCategoryRequest;
import org.example.dto.category.CategoryResponse;
import org.example.model.Category;
import org.example.service.CategoryService;
import org.example.service.exception.CategoryNotFoundException;
import org.example.service.exception.DuplicateCategoryException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryDAO categoryDAO;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Should throw an error when category name already exists")
    void testCreateExistingCategory() throws DAOException {
        CreateCategoryRequest request = new CreateCategoryRequest("name", "description");
        Category existing = new Category(
                UUID.randomUUID(),
                "name",
                "description",
                Instant.now(),
                Instant.now()
        );

        when(categoryDAO.findByName(any())).thenReturn(Optional.of(existing));

        Assertions.assertThrows(DuplicateCategoryException.class, () -> {
            categoryService.createCategory(request);
        });
    }

    @Test
    @DisplayName("Should create a new category and return a response")
    void testCreateNewCategory() throws DAOException {
        CreateCategoryRequest request = new CreateCategoryRequest("name", "description");

        when(categoryDAO.findByName(any())).thenReturn(Optional.empty());
        CategoryResponse response = categoryService.createCategory(request);

        String expected = request.name();
        String actual = response.name();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should throw NotFoundException when category not found")
    void testGetNonExistingCategoryById() throws DAOException{
        UUID randomId = UUID.randomUUID();

        when(categoryDAO.findById(randomId)).thenReturn(Optional.empty());

        Assertions.assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.getCategory(randomId);
        });
    }

    @Test
    @DisplayName("Should get an existing category with id")
    void testGetExistingCategoryById() throws DAOException {
        UUID id = UUID.randomUUID();
        Category existing = new Category(
                id,
                "name",
                "description",
                Instant.now(),
                Instant.now()
        );

        when(categoryDAO.findById(id)).thenReturn(Optional.of(existing));
        var response = categoryService.getCategory(id);

        UUID expected = existing.getCategoryId();
        UUID actual = response.categoryId();

        Assertions.assertEquals(expected, actual);
    }


    @Test
    @DisplayName("Should throw NotFoundException when category name not found")
    void testGetNonExistingCategoryByName() throws DAOException {
        String randomName = "non-existent-category";

        when(categoryDAO.findByName(randomName)).thenReturn(Optional.empty());

        Assertions.assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.getCategory(randomName);
        });
    }

    @Test
    @DisplayName("Should get an existing category with name")
    void testGetExistingCategoryByName() throws DAOException {
        String name = "electronics";
        Category existing = new Category(
                UUID.randomUUID(),
                name,
                "description",
                Instant.now(),
                Instant.now()
        );

        when(categoryDAO.findByName(name)).thenReturn(Optional.of(existing));

        var response = categoryService.getCategory(name);

        String expectedName = existing.getName();
        String actualName = response.name();

        Assertions.assertEquals(expectedName, actualName);
    }
}
