package org.example.dao.impl;

import org.example.dao.interfaces.ProductDAO;
import org.example.dao.interfaces.StatementPreparer;
import org.example.dao.exception.DAOException;
import org.example.dao.exception.InsufficientStockException;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.util.data.DBConnection;
import org.example.util.data.SqlAndParams;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductJdbcDAO implements ProductDAO {

    private static final String FIND_BY_ID = """
            SELECT * FROM product WHERE product_id = ?
            """;

    private static final String FIND_ALL = """
            SELECT * FROM product
            ORDER BY name ASC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_ALL = """
            SELECT COUNT(*) FROM product
            """;

    private static final String FILTER = """
            SELECT * FROM product p
            """;

    private static final String FIND_BY_CATEGORY = """
            SELECT * FROM product
            WHERE category_id = ?
            LIMIT ? OFFSET ?
            """;

    private static final String SEARCH_BY_NAME = """
            SELECT * FROM product
            WHERE LOWER(name) LIKE LOWER(?)
            ORDER BY name ASC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_BY_NAME = """
            SELECT COUNT(*) FROM product
            WHERE LOWER(name) LIKE LOWER(?)
            """;

    private static final String FILTER_COUNT = """
            SELECT COUNT(*)
            FROM product p
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
    public Optional<Product> findById(UUID productId) throws DAOException {
        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(FIND_BY_ID);

            preparedStatement.setObject(1, productId);

            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    return Optional.of(mapRowToProduct(resultSet));
            } catch (SQLException e) {
                throw new DAOException("Failed to find product " + productId, e);
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to find product " + productId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> findById(Connection conn, UUID productId) throws DAOException {
        try(PreparedStatement preparedStatement = conn.prepareStatement(FIND_BY_ID)) {
            preparedStatement.setObject(1, productId);

            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    return Optional.of(mapRowToProduct(resultSet));
            } catch (SQLException e) {
                throw new DAOException("Failed to find product " + productId, e);
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to find product " + productId, e);
        }
        return Optional.empty();
    }

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

    @Override
    public List<Product> findAll(int limit, int offset) throws DAOException {
        try {
            return queryList(FIND_ALL, ps -> {
                ps.setInt(1, limit);
                ps.setInt(2, offset);
            });
        } catch (DatabaseConnectionException | SQLException e) {
            throw new DAOException("Failed to load all products", e);
        }
    }

    private List<Product> queryList(String query, StatementPreparer preparer) throws SQLException,
            DatabaseConnectionException {
        List<Product> results = new ArrayList<>();

        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(query);

            if (preparer != null)
                preparer.prepare(preparedStatement);

            try(ResultSet set = preparedStatement.executeQuery()) {
                while(set.next()) {
                    results.add(mapRowToProduct(set));
                }
            }
        }

        return results;
    }

    @Override
    public int countAll() throws DAOException {
        try(Connection conn = DBConnection.getConnection()){
            PreparedStatement statement = conn.prepareStatement(COUNT_ALL);
            try(ResultSet rs = statement.executeQuery()){
                if(rs.next()) {
                    long rowCount = rs.getLong(1);
                    if(rowCount > Integer.MAX_VALUE)
                        throw new DAOException("Product count exceeds integer range:" + rowCount, null);
                    return (int) rowCount;
                } else {
                    return 0;
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to get product count", e);
        }
    }

    @Override
    public List<Product> findByCategory(UUID categoryId, int limit, int offset) throws DAOException {
        try {
            return queryList(FIND_BY_CATEGORY, ps -> {
                ps.setObject(1, categoryId);
                ps.setInt(2, limit);
                ps.setInt(3, offset);
            });
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to load from category" + categoryId, e);
        }
    }

    @Override
    public List<Product> searchByName(String query, int limit, int offset) throws DAOException {
        try {
            return queryList(SEARCH_BY_NAME, ps -> {
                ps.setString(1, "%" + query + "%");
                ps.setInt(2, limit);
                ps.setInt(3, offset);
            });
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to retrieve products with name" + query, e);
        }
    }

    @Override
    public List<Product> findFiltered(ProductFilter filter, int limit, int offset) throws DAOException {
        SqlAndParams where = buildWhereClause(filter);
        String finalSql = FILTER + where.sql() + " ORDER BY p.name ASC LIMIT ? OFFSET ?";

        List<Product> results = new ArrayList<>();

        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(finalSql);

            int nextIndex = buildParams(ps, where);
            ps.setInt(nextIndex++, limit);
            ps.setInt(nextIndex, offset);

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    results.add(mapRowToProduct(rs));
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to fetch products", e);
        }

        return results;
    }

    private int buildParams(PreparedStatement ps, SqlAndParams where) throws SQLException{
        int index = 1;
        for (Object param : where.params()) {
            ps.setObject(index++, param);
        }

        return index;
    }

    private SqlAndParams buildWhereClause(ProductFilter filter) {
        StringBuilder sql = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if(filter.hasName() && !filter.name().isBlank()) {
            sql.append(" AND p.name ILIKE ? ");
            params.add("%" + filter.name() + "%");
        }

        if(filter.hasCategoryId()) {
            sql.append(" AND p.category_id = ?");
            params.add(filter.categoryId());
        }

        return new SqlAndParams(sql.toString(), params);
    }

    @Override
    public int countByName(String query) throws DAOException {
        try(Connection conn = DBConnection.getConnection()){
            PreparedStatement ps = conn.prepareStatement(COUNT_BY_NAME);
            ps.setString(1, "%" + query + "%");

            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long rowCount = rs.getLong(1);
                    if(rowCount > Integer.MAX_VALUE)
                        throw new DAOException("Product count exceeds integer range:" + rowCount, null);
                    return (int) rowCount;
                } else {
                    return 0;
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to count products by name", e);
        }
    }

    @Override
    public int countFiltered(ProductFilter filter) throws DAOException {
        SqlAndParams where = buildWhereClause(filter);
        String finalSql = FILTER_COUNT + where.sql();

        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(finalSql);
            buildParams(ps, where);

            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long rowCount = rs.getLong(1);
                    if(rowCount > Integer.MAX_VALUE)
                        throw new DAOException("Product count exceeds integer range:" + rowCount, null);
                    return (int) rowCount;
                } else {
                    return 0;
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to count products", e);
        }
    }

    @Override
    public void save(Product product) throws DAOException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SAVE)) {

            ps.setObject(1, product.getProductId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getStockQuantity());
            ps.setObject(6, product.getCategoryId());
            ps.setTimestamp(7, Timestamp.from(product.getCreatedAt()));
            ps.setTimestamp(8, Timestamp.from(product.getUpdatedAt()));

            ps.executeUpdate();
        } catch (DatabaseConnectionException | SQLException e) {
            throw new DAOException("Error saving product", e);
        }
    }

    @Override
    public void reduceStock(Connection conn, UUID productId, int quantity) throws DAOException {
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
    public void increaseStock(Connection conn, UUID productId, int quantity) throws DAOException {
        try(PreparedStatement preparedStatement = conn.prepareStatement(INCREASE_STOCK);){
            preparedStatement.setInt(1, quantity);
            preparedStatement.setObject(2, productId);
            preparedStatement.setInt(3, quantity);

            int updateRows = preparedStatement.executeUpdate();
            if(updateRows == 0)
                throw new InsufficientStockException(productId.toString());

        } catch (SQLException e) {
            throw new DAOException("Failed to increase stock for product" + productId, e);
        }
    }

    @Override
    public void update(Product product) throws DAOException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.setObject(5, product.getCategoryId());
            ps.setTimestamp(6, Timestamp.from(product.getUpdatedAt()));
            ps.setObject(7, product.getProductId());

            ps.executeUpdate();
        } catch (DatabaseConnectionException | SQLException e) {
            throw new DAOException("Error updating product", e);
        }
    }

    @Override
    public void deleteById(UUID productId) throws DAOException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {

            ps.setObject(1, productId);
            ps.executeUpdate();
        } catch (DatabaseConnectionException | SQLException e) {
            throw new RuntimeException("Error deleting product", e);
        }
    }
}