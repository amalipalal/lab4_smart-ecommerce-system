import org.example.dto.product.CreateProductRequest;
import org.example.dto.product.CreateProductResponse;
import org.example.dto.product.ProductResponse;
import org.example.dto.product.UpdateProductRequest;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.service.ProductService;
import org.example.service.exception.ProductNotFoundException;
import org.example.store.ProductStore;
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
class ProductServiceTest {

    @Mock
    private ProductStore productStore;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {
        CreateProductRequest request = new CreateProductRequest(
                "Laptop", "Desc", 1200.0, 5, UUID.randomUUID()
        );
        Product savedProduct = new Product(
                UUID.randomUUID(), "Laptop", "Desc", 1200.0, 5,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        when(productStore.createProduct(any(Product.class))).thenReturn(savedProduct);

        CreateProductResponse response = productService.createProduct(request);

        Assertions.assertEquals("Laptop", response.name());
        verify(productStore).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("Should get product by id successfully")
    void shouldGetProductByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        Product product = new Product(
                id, "Phone", "Desc", 800.0, 10,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        when(productStore.getProduct(id)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProduct(id);

        Assertions.assertEquals(id, response.productId());
        Assertions.assertEquals("Phone", response.name());
        verify(productStore).getProduct(id);
    }

    @Test
    @DisplayName("Should throw error when product not found by id")
    void shouldThrowWhenProductNotFoundById() {
        UUID id = UUID.randomUUID();

        when(productStore.getProduct(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProduct(id)
        );

        verify(productStore).getProduct(id);
    }

    @Test
    @DisplayName("Should delete product successfully")
    void shouldDeleteProductSuccessfully() {
        UUID id = UUID.randomUUID();
        Product product = new Product(
                id, "Laptop", "Desc", 1200.0, 5,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        when(productStore.getProduct(id)).thenReturn(Optional.of(product));

        productService.deleteProduct(id);

        verify(productStore).getProduct(id);
        verify(productStore).deleteProduct(id);
    }

    @Test
    @DisplayName("Should throw error when deleting non-existing product")
    void shouldThrowWhenDeletingNonExistingProduct() {
        UUID id = UUID.randomUUID();

        when(productStore.getProduct(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(id)
        );

        verify(productStore).getProduct(id);
        verify(productStore, never()).deleteProduct(any());
    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProductSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest("New Name", "New Desc", 1500.0, categoryId, 8);
        Product existing = new Product(
                id, "Old Name", "Old Desc", 1200.0, 5,
                UUID.randomUUID(), Instant.now(), Instant.now()
        );

        when(productStore.getProduct(id)).thenReturn(Optional.of(existing));

        productService.updateProduct(id, request);

        verify(productStore).getProduct(id);
        verify(productStore).updateProduct(argThat(product ->
                product.getName().equals("New Name") &&
                product.getDescription().equals("New Desc") &&
                product.getPrice() == 1500.0 &&
                product.getStockQuantity() == 8
        ));
    }

    @Test
    @DisplayName("Should throw error when updating non-existing product")
    void shouldThrowWhenUpdatingMissingProduct() {
        UUID id = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest("New", null, null, null, null);

        when(productStore.getProduct(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(id, request)
        );

        verify(productStore).getProduct(id);
        verify(productStore, never()).updateProduct(any());
    }

    @Test
    @DisplayName("Should update only provided fields")
    void shouldUpdateOnlyProvidedFields() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest("New Name", null, null, null, null);
        Product existing = new Product(
                id, "Old Name", "Old Desc", 1200.0, 5,
                categoryId, Instant.now(), Instant.now()
        );

        when(productStore.getProduct(id)).thenReturn(Optional.of(existing));

        productService.updateProduct(id, request);

        verify(productStore).updateProduct(argThat(product ->
                product.getName().equals("New Name") &&
                product.getDescription().equals("Old Desc") &&
                product.getPrice() == 1200.0 &&
                product.getStockQuantity() == 5 &&
                product.getCategoryId().equals(categoryId)
        ));
    }

    @Test
    @DisplayName("Should search products successfully")
    void shouldSearchProductsSuccessfully() {
        ProductFilter filter = new ProductFilter("Laptop", null);
        List<Product> products = List.of(
                new Product(UUID.randomUUID(), "Laptop", "Desc", 1200.0, 5, UUID.randomUUID(), Instant.now(), Instant.now())
        );

        when(productStore.searchProducts(filter, 10, 0)).thenReturn(products);

        List<ProductResponse> result = productService.searchProducts(filter, 10, 0);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Laptop", result.getFirst().name());
        verify(productStore).searchProducts(filter, 10, 0);
    }

    @Test
    @DisplayName("Should return empty list when no products match filter")
    void shouldReturnEmptyListWhenNoProductsMatchFilter() {
        ProductFilter filter = new ProductFilter("NonExistent", null);

        when(productStore.searchProducts(filter, 10, 0)).thenReturn(List.of());

        List<ProductResponse> result = productService.searchProducts(filter, 10, 0);

        Assertions.assertEquals(0, result.size());
        verify(productStore).searchProducts(filter, 10, 0);
    }

    @Test
    @DisplayName("Should count products by filter successfully")
    void shouldCountProductsByFilterSuccessfully() {
        ProductFilter filter = new ProductFilter(null, UUID.randomUUID());

        when(productStore.countProductsByFilter(filter)).thenReturn(15);

        int count = productService.countProductsByFilter(filter);

        Assertions.assertEquals(15, count);
        verify(productStore).countProductsByFilter(filter);
    }

    @Test
    @DisplayName("Should preserve created timestamp when updating")
    void shouldPreserveCreatedTimestampWhenUpdating() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(86400);
        UpdateProductRequest request = new UpdateProductRequest("New Name", null, null, null, null);
        Product existing = new Product(
                id, "Old Name", "Desc", 1200.0, 5,
                UUID.randomUUID(), createdAt, Instant.now()
        );

        when(productStore.getProduct(id)).thenReturn(Optional.of(existing));

        productService.updateProduct(id, request);

        verify(productStore).updateProduct(argThat(product ->
                product.getCreatedAt().equals(createdAt)
        ));
    }

    @Test
    @DisplayName("Should handle pagination in search")
    void shouldHandlePaginationInSearch() {
        ProductFilter filter = new ProductFilter("Phone", null);
        List<Product> products = List.of(
                new Product(UUID.randomUUID(), "Phone1", "Desc", 800.0, 10, UUID.randomUUID(), Instant.now(), Instant.now()),
                new Product(UUID.randomUUID(), "Phone2", "Desc", 900.0, 8, UUID.randomUUID(), Instant.now(), Instant.now())
        );

        when(productStore.searchProducts(filter, 5, 10)).thenReturn(products);

        List<ProductResponse> result = productService.searchProducts(filter, 5, 10);

        Assertions.assertEquals(2, result.size());
        verify(productStore).searchProducts(filter, 5, 10);
    }

}
