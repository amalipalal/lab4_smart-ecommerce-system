package org.example.dao.impl.product;

import org.example.dao.exception.DAOException;
import org.example.dao.exception.InsufficientStockException;
import org.example.dao.interfaces.product.ProductWriteDao;
import org.example.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class SqlProductWriteDao implements ProductWriteDao {
    private final Connection conn;

    private static final String SAVE = """
            INSERT INTO product (
                product_id, name, description, price,
                stock_quantity, category_id, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE product
            SET name = ?, description = ?, price = ?, stock_quantity = ?,
                category_id = ?, updated_at = ?
            WHERE product_id = ?
            """;

    private static final String REDUCE_STOCK = """
            UPDATE product
            SET stock_quantity = stock_quantity - ?
            WHERE product_id = ? AND stock_quantity >= ?
            """;

    private static final String INCREASE_STOCK = """
            UPDATE product
            SET stock_quantity = stock_quantity + ?
            WHERE product_id = ? AND stock_quantity >= ?
            """;

    private static final String DELETE = """
            DELETE FROM product WHERE product_id = ?
            """;

    public SqlProductWriteDao(Connection connection) {
        this.conn = connection;
    }

    @Override
    public void save(Product product) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(SAVE)) {
            ps.setObject(1, product.getProductId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getStockQuantity());
            ps.setObject(6, product.getCategoryId());
            ps.setTimestamp(7, Timestamp.from(product.getCreatedAt()));
            ps.setTimestamp(8, Timestamp.from(product.getUpdatedAt()));

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error saving product", e);
        }
    }

    @Override
    public void update(Product product) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.setObject(5, product.getCategoryId());
            ps.setTimestamp(6, Timestamp.from(product.getUpdatedAt()));
            ps.setObject(7, product.getProductId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error updating product", e);
        }
    }

    @Override
    public void reduceStock(UUID productId, int quantity) throws DAOException {
        try(PreparedStatement preparedStatement = conn.prepareStatement(REDUCE_STOCK)){
            preparedStatement.setInt(1, quantity);
            preparedStatement.setObject(2, productId);
            preparedStatement.setInt(3, quantity);

            int updateRows = preparedStatement.executeUpdate();
            if(updateRows == 0)
                throw new InsufficientStockException(productId.toString());

        } catch (SQLException e) {
            throw new DAOException("Failed to update stock for product" + productId, e);
        }
    }

    @Override
    public void increaseStock(UUID productId, int quantity) throws DAOException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(INCREASE_STOCK);) {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setObject(2, productId);
            preparedStatement.setInt(3, quantity);

            int updateRows = preparedStatement.executeUpdate();
            if (updateRows == 0)
                throw new InsufficientStockException(productId.toString());

        } catch (SQLException e) {
            throw new DAOException("Failed to increase stock for product" + productId, e);
        }
    }

    @Override
    public void deleteById(UUID productId) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE)) {

            ps.setObject(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product", e);
        }
    }
}
