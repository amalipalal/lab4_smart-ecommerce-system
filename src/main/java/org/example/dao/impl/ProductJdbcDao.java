package org.example.dao.impl;

import org.example.dao.exception.DAOException;
import org.example.dao.exception.InsufficientStockException;
import org.example.dao.interfaces.ProductDao;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.util.SqlAndParams;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductJdbcDao implements ProductDao {

    private static final String FIND_BY_ID = """
            SELECT product_id, name, description, price, stock_quantity,
                   category_id, created_at, updated_at
            FROM product WHERE product_id = ?
            """;

    private static final String FIND_ALL = """
            SELECT procuct_id, name, description, price,
                   stock_quantity, category_id, created_at, updated_at
            FROM product
            ORDER BY LOWER(name) ASC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_ALL = """
            SELECT COUNT(*) FROM product
            """;

    private static final String FILTER = """
            SELECT p.product_id, p.name, p.description, p.price,
                   p.stock_quantity, p.category_id, p.created_at, p.updated_at
            FROM product p
            JOIN category c ON c.category_id = p.category_id
            """;

    private static final String FILTER_COUNT = """
            SELECT COUNT(*)
            FROM product p
            JOIN category c ON c.category_id = p.category_id
            """;

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

    @Override
    public Optional<Product> findById(Connection conn, UUID productId) throws DAOException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(FIND_BY_ID)) {
            preparedStatement.setObject(1, productId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToProduct(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to find product " + productId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll(Connection conn, int limit, int offset) throws DAOException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(FIND_ALL)) {
            preparedStatement.setInt(1, limit);
            preparedStatement.setInt(2, offset);

            return executeQueryForList(preparedStatement);
        } catch (SQLException e) {
            throw new DAOException("Failed to load all products", e);
        }
    }

    @Override
    public int countAll(Connection conn) throws DAOException {
        try (PreparedStatement statement = conn.prepareStatement(COUNT_ALL)) {
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    long rowCount = rs.getLong(1);
                    if (rowCount > Integer.MAX_VALUE) {
                        throw new DAOException("Product count exceeds integer range:" + rowCount, null);
                    }
                    return (int) rowCount;
                } else {
                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to get product count", e);
        }
    }

    @Override
    public List<Product> findFiltered(Connection conn, ProductFilter filter, int limit, int offset) throws DAOException {
        SqlAndParams where = buildWhereClause(filter);
        String finalSql = FILTER + where.sql() + " ORDER BY p.name ASC LIMIT ? OFFSET ?";

        try (PreparedStatement ps = conn.prepareStatement(finalSql)) {
            int nextIndex = setParameters(ps, where.params());
            ps.setInt(nextIndex++, limit);
            ps.setInt(nextIndex, offset);

            return executeQueryForList(ps);
        } catch (SQLException e) {
            throw new DAOException("Failed to fetch filtered products", e);
        }
    }

    @Override
    public int countFiltered(Connection conn, ProductFilter filter) throws DAOException {
        SqlAndParams where = buildWhereClause(filter);
        String finalSql = FILTER_COUNT + where.sql();

        try (PreparedStatement ps = conn.prepareStatement(finalSql)) {
            setParameters(ps, where.params());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long rowCount = rs.getLong(1);
                    if (rowCount > Integer.MAX_VALUE) {
                        throw new DAOException("Product count exceeds integer range:" + rowCount, null);
                    }
                    return (int) rowCount;
                } else {
                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to count filtered products", e);
        }
    }

    @Override
    public void save(Connection conn, Product product) throws DAOException {
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
    public void update(Connection conn, Product product) throws DAOException {
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
    public void reduceStock(Connection conn, UUID productId, int quantity) throws DAOException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(REDUCE_STOCK)) {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setObject(2, productId);
            preparedStatement.setInt(3, quantity);

            int updateRows = preparedStatement.executeUpdate();
            if (updateRows == 0) {
                throw new InsufficientStockException(productId.toString());
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to reduce stock for product " + productId, e);
        }
    }

    @Override
    public void increaseStock(Connection conn, UUID productId, int quantity) throws DAOException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(INCREASE_STOCK)) {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setObject(2, productId);
            preparedStatement.setInt(3, quantity);

            int updateRows = preparedStatement.executeUpdate();
            if (updateRows == 0) {
                throw new InsufficientStockException(productId.toString());
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to increase stock for product " + productId, e);
        }
    }

    @Override
    public void deleteById(Connection conn, UUID productId) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setObject(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error deleting product", e);
        }
    }

    // Helper methods

    private Product mapRowToProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getObject("product_id", UUID.class),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getDouble("price"),
                resultSet.getInt("stock_quantity"),
                resultSet.getObject("category_id", UUID.class),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("updated_at").toInstant()
        );
    }

    private List<Product> executeQueryForList(PreparedStatement ps) throws SQLException {
        List<Product> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRowToProduct(rs));
            }
        }
        return results;
    }

    private int setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        int index = 1;
        for (Object param : params) {
            ps.setObject(index++, param);
        }
        return index;
    }

    private SqlAndParams buildWhereClause(ProductFilter filter) {
        StringBuilder sql = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (filter.hasName() && !filter.name().isBlank()) {
            sql.append(" AND p.name ILIKE ? ");
            params.add("%" + filter.name() + "%");
        }

        if (filter.hasCategoryId()) {
            sql.append(" AND p.category_id = ?");
            params.add(filter.categoryId());
        }

        return new SqlAndParams(sql.toString(), params);
    }
}
