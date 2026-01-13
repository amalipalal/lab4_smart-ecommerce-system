import org.example.UnitOfWorkFactory;
import org.example.cache.ProductCache;
import org.example.dao.interfaces.CategoryWriteDaoFactory;
import org.example.dao.interfaces.UnitOfWork;
import org.example.dao.interfaces.category.CategoryReadDao;
import org.example.dao.interfaces.category.CategoryWriteDao;
import org.example.dto.category.CreateCategoryRequest;
import org.example.dto.category.UpdateCategoryRequest;
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

import java.sql.Connection;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryReadDao readDao;

    @Mock
    private CategoryWriteDao writeDao;

    @Mock
    private UnitOfWorkFactory unitOfWorkFactory;

    @Mock
    private UnitOfWork unitOfWork;

    @Mock
    private Connection connection;

    @Mock
    private ProductCache cache;

    @Mock
    private CategoryWriteDaoFactory writeDaoFactory;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Should throw error when creating a category with duplicate name")
    void shouldThrowWhenCreatingDuplicateCategory() {
        CreateCategoryRequest request =
                new CreateCategoryRequest("electronics", "desc");

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(writeDaoFactory.create(connection)).thenReturn(writeDao);
        when(readDao.findByName("electronics"))
                .thenReturn(Optional.of(mock(Category.class)));

        Assertions.assertThrows(
                DuplicateCategoryException.class,
                () -> categoryService.createCategory(request)
        );
    }

    @Test
    @DisplayName("Should create category successfully")
    void shouldCreateCategorySuccessfully() {
        CreateCategoryRequest request =
                new CreateCategoryRequest("books", "desc");

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(writeDaoFactory.create(connection)).thenReturn(writeDao);
        when(readDao.findByName(any())).thenReturn(Optional.empty());

        var response = categoryService.createCategory(request);

        Assertions.assertEquals("books", response.name());
        verify(unitOfWork).commit();
        verify(cache).invalidateByPrefix("category:");
    }

    @Test
    @DisplayName("Should throw error when updating non-existing category")
    void shouldThrowWhenUpdatingMissingCategory() {
        UUID id = UUID.randomUUID();
        UpdateCategoryRequest request =
                new UpdateCategoryRequest(id, "new", "desc");

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(writeDaoFactory.create(connection)).thenReturn(writeDao);
        when(readDao.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.updateCategory(request)
        );
    }


    @Test
    @DisplayName("Should throw error when category not found by id")
    void shouldThrowWhenCategoryNotFoundById() {
        UUID id = UUID.randomUUID();

        when(cache.getOrLoad(any(), any()))
                .thenThrow(new CategoryNotFoundException(id.toString()));

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getCategory(id)
        );
    }

    @Test
    @DisplayName("Should return category when found by id")
    void shouldReturnCategoryById() {
        UUID id = UUID.randomUUID();
        Category category = new Category(
                id, "games", "desc", Instant.now(), Instant.now()
        );

        when(cache.getOrLoad(any(), any())).thenReturn(category);

        var response = categoryService.getCategory(id);

        Assertions.assertEquals(id, response.categoryId());
    }

    @Test
    @DisplayName("Should throw error when category not found by name")
    void shouldThrowWhenCategoryNotFoundByName() {
        when(cache.getOrLoad(any(), any()))
                .thenThrow(new CategoryNotFoundException("missing"));

        Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getCategory("missing")
        );
    }
}
