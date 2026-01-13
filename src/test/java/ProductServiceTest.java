import org.example.persistence.UnitOfWorkFactory;
import org.example.cache.ProductCache;
import org.example.dao.interfaces.ProductWriteDaoFactory;
import org.example.persistence.UnitOfWork;
import org.example.dao.interfaces.product.ProductReadDao;
import org.example.dao.interfaces.product.ProductWriteDao;
import org.example.dto.product.CreateProductRequest;
import org.example.dto.product.UpdateProductRequest;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.service.ProductService;
import org.example.service.exception.ProductNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductReadDao productReadDao;

    @Mock
    private ProductWriteDao productWriteDao;

    @Mock
    private ProductWriteDaoFactory productWriteDaoFactory;

    @Mock
    private UnitOfWorkFactory unitOfWorkFactory;

    @Mock
    private UnitOfWork unitOfWork;

    @Mock
    private Connection connection;

    @Mock
    private ProductCache cache;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {
        CreateProductRequest request = new CreateProductRequest(
                "Laptop", "Desc", 1200.0, 5, UUID.randomUUID()
        );

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(productWriteDaoFactory.create(connection)).thenReturn(productWriteDao);

        var response = productService.createProduct(request);

        Assertions.assertEquals("Laptop", response.name());
        verify(productWriteDao).save(any());
        verify(unitOfWork).commit();
        verify(cache).invalidateByPrefix("product:");
    }

    @Test
    @DisplayName("Should throw error when product not found by id")
    void shouldThrowWhenProductNotFoundById() {
        UUID id = UUID.randomUUID();

        when(cache.getOrLoad(any(), any()))
                .thenThrow(new ProductNotFoundException(id.toString()));

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProduct(id)
        );
    }

    @Test
    @DisplayName("Should return product when found by id")
    void shouldReturnProductById() {
        UUID id = UUID.randomUUID();
        Product product = new Product(
                id, "Phone", "Desc", 800.0, 10,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        when(cache.getOrLoad(any(), any())).thenReturn(product);

        var response = productService.getProduct(id);

        Assertions.assertEquals(id, response.productId());
    }

    @Test
    @DisplayName("Should delete product successfully")
    void shouldDeleteProductSuccessfully() {
        UUID id = UUID.randomUUID();

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(productWriteDaoFactory.create(connection)).thenReturn(productWriteDao);

        productService.deleteProduct(id);

        verify(productWriteDao).deleteById(id);
        verify(unitOfWork).commit();
        verify(cache).invalidateByPrefix("product:");
    }

    @Test
    @DisplayName("Should throw error when updating non-existing product")
    void shouldThrowWhenUpdatingMissingProduct() {
        UUID id = UUID.randomUUID();
        UpdateProductRequest request =
                new UpdateProductRequest("New", null, null, null, null);

        when(unitOfWorkFactory.create()).thenReturn(unitOfWork);
        when(unitOfWork.getConnection()).thenReturn(connection);
        when(productWriteDaoFactory.create(connection)).thenReturn(productWriteDao);

        when(cache.getOrLoad(any(), any()))
                .thenAnswer(invocation -> {
                    var loader = invocation.getArgument(1, Supplier.class);
                    return loader.get();
                });

        when(productReadDao.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(id, request)
        );
    }

    @Test
    @DisplayName("Should return products for filter search")
    void shouldSearchProductsSuccessfully() {
        ProductFilter filter = mock(ProductFilter.class);

        when(cache.getOrLoad(any(), any()))
                .thenReturn(List.of());

        var result = productService.searchProducts(filter, 10, 0);

        Assertions.assertNotNull(result);
    }
}
