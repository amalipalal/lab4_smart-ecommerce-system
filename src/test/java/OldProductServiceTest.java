import org.example.dao.interfaces.product.ProductDAO;
import org.example.dao.exception.DAOException;
import org.example.dto.product.CreateProductRequest;
import org.example.model.Product;
import org.example.service.OldProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OldProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    @InjectMocks
    private OldProductService oldProductService;

    @Test
    void testThrowsDaoWhenCreatingProduct() throws DAOException {
        var request = new CreateProductRequest("name", "description",
                12.05, 2, UUID.randomUUID());

        doThrow(new DAOException("save failed", new SQLException())).when(productDAO).save(any(Product.class));

        Assertions.assertThrows(RuntimeException.class, () -> {
            oldProductService.createProduct(request);
        });
        verify(productDAO, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct() throws DAOException {
        var request = new CreateProductRequest("name", "description",
                12.05, 2, UUID.randomUUID());

        var response = oldProductService.createProduct(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productDAO, times(1)).save(captor.capture());

        String expectedName = request.name();
        String actualName = captor.getValue().getName();

        Assertions.assertEquals(expectedName, actualName);
    }

    @Test
    void testGetProductById() throws DAOException {
        var product = new Product(UUID.randomUUID(), "name", "description", 12.45, 2, UUID.randomUUID(),
                Instant.now(), Instant.now());

        when(productDAO.findById(product.getProductId())).thenReturn(Optional.of(product));

        var response = oldProductService.getProduct(product.getProductId());

        UUID expectedId = product.getProductId();
        UUID actualId = response.productId();

        Assertions.assertEquals(expectedId, actualId);
    }
}
