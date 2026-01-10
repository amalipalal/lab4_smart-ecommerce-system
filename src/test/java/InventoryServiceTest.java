import org.example.dao.interfaces.ProductDAO;
import org.example.dao.exception.DAOException;
import org.example.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private ProductDAO productDAO;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("Should call DAO to reduce stock")
    void testReduceStockWithValidInput() throws DAOException {
        UUID productId = UUID.randomUUID();
        int quantity = 5;

        inventoryService.reduceStock(productId, quantity);

        verify(productDAO, times(1)).reduceStock(productId, quantity);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when quantity is invalid")
    void testReduceStockWithInvalidInput() {
        UUID productId = UUID.randomUUID();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.reduceStock(productId, 0)
        );

        assertEquals("Quantity must be positive", ex.getMessage());
        verifyNoInteractions(productDAO);
    }

    @Test
    @DisplayName("Should handle DAOException with RuntimeException on reduce stock")
    void reduceStock_shouldWrapDAOException_inRuntimeException() throws DAOException {
        UUID productId = UUID.randomUUID();
        int quantity = 3;

        doThrow(new DAOException("DB error", new SQLException()))
                .when(productDAO)
                .reduceStock(productId, quantity);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> inventoryService.reduceStock(productId, quantity)
        );

        assertEquals("Inventory update failed", ex.getMessage());
        assertInstanceOf(DAOException.class, ex.getCause());
    }

    @Test
    @DisplayName("Should call DAO when inputs are valid")
    void increaseStock_shouldCallDAO_whenInputsAreValid() throws DAOException {
        UUID productId = UUID.randomUUID();
        int quantity = 10;

        inventoryService.increaseStock(productId, quantity);

        verify(productDAO, times(1)).increaseStock(productId, quantity);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when input is invalid")
    void increaseStock_shouldThrowIllegalArgumentException_whenQuantityIsZeroOrNegative() {
        UUID productId = UUID.randomUUID();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.increaseStock(productId, -2)
        );

        assertEquals("Quantity must be positive", ex.getMessage());
        verifyNoInteractions(productDAO);
    }

    @Test
    @DisplayName("Should handle DAOException with RuntimeException on increase stock")
    void increaseStock_shouldWrapDAOException_inRuntimeException() throws DAOException {
        UUID productId = UUID.randomUUID();
        int quantity = 4;

        doThrow(new DAOException("DB error", new SQLException()))
                .when(productDAO)
                .increaseStock(productId, quantity);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> inventoryService.increaseStock(productId, quantity)
        );

        assertEquals("Inventory increase failed", ex.getMessage());
        assertInstanceOf(DAOException.class, ex.getCause());
    }
}
