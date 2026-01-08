package org.example.dao.impl;

import org.example.dao.CategoryDAO;
import org.example.dao.StatementPreparer;
import org.example.dao.exception.DAOException;
import org.example.model.Category;
import org.example.util.DBConnection;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryJdbcDAO implements CategoryDAO {

    private static  final String FIND_BY_ID = """
            SELECT * FROM category
            WHERE category_id = ?
            """;

    private static final String FIND_BY_NAME = """
            SELECT * FROM category
            WHERE name = ?
            """;

    private static final String SEARCH_BY_NAME = """
            SELECT *
            FROM category
            WHERE LOWER(name) LIKE LOWER(?)
            ORDER BY name ASC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_BY_NAME = """
        SELECT COUNT(*)
        FROM category
        WHERE LOWER(name) LIKE LOWER(?)
        """;

    private static final String FIND_ALL = """
            SELECT * FROM category
            ORDER BY name ASC
            LIMIT ? OFFSET ?
            """;

    private static final String SAVE = """
            INSERT INTO category (
            category_id, name, description, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE category
            SET name = ?, description = ?, updated_at = ?
            WHERE category_id = ?
            """;

    private static final String COUNT = """
            SELECT COUNT(*) FROM category
            """;

    @Override
    public Optional<Category> findById(UUID categoryId) throws DAOException {
        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(FIND_BY_ID);

            ps.setObject(1, categoryId);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return Optional.of(map(rs));
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to fetch category " + categoryId, e);
        }

        return Optional.empty();
    }

    private Category map(ResultSet resultSet) throws SQLException{
        return new Category(
                resultSet.getObject("category_id", UUID.class),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("updated_at").toInstant()
        );
    }

    @Override
    public Optional<Category> findByName(String name) throws DAOException {
        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(FIND_BY_NAME);

            ps.setObject(1, name);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return Optional.of(map(rs));
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to fetch category " + name, e);
        }

        return Optional.empty();
    }

    @Override
    public List<Category> searchByName(String query, int limit, int offset) throws DAOException {
        List<Category> categories = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SEARCH_BY_NAME);

            ps.setString(1, "%" + query + "%");
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(map(rs));
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to search categories by name", e);
        }

        return categories;
    }

    @Override
    public int countByName(String query) throws DAOException {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(COUNT_BY_NAME);
            ps.setString(1, "%" + query + "%");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to count categories by name", e);
        }
    }

    @Override
    public List<Category> findAll(int limit, int offset) throws DAOException {
        List<Category> categories = new ArrayList<>();

        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(FIND_ALL);

            ps.setObject(1, limit);
            ps.setObject(2, offset);
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    categories.add(map(rs));
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to fetch all categories" + e.getMessage(), e);
        }
        return categories;
    }

    @Override
    public void save(Category category) throws DAOException {
        try {
            insertionQuery(SAVE, ps -> {
                ps.setObject(1, category.getCategoryId());
                ps.setString(2, category.getName());
                ps.setString(3, category.getDescription());
                ps.setTimestamp(4, Timestamp.from(category.getCreatedAt()));
                ps.setTimestamp(5, Timestamp.from(category.getUpdatedAt()));
            });
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to save " + category.getName() + "category.", e);
        }
    }

    private void insertionQuery(String query, StatementPreparer preparer) throws SQLException,
            DatabaseConnectionException {
        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);

            if(preparer != null) preparer.prepare(ps);

            ps.executeUpdate();
        }
    }

    @Override
    public void update(Category category) throws DAOException {
        try {
            insertionQuery(UPDATE, ps -> {
                ps.setString(1, category.getName());
                ps.setString(2, category.getDescription());
                ps.setTimestamp(3, Timestamp.from(category.getUpdatedAt()));
                ps.setObject(4, category.getCategoryId());
            });
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to update " + category.getName() + "category.", e);
        }
    }

    @Override
    public int count() throws DAOException {
        try(Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(COUNT);

            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    long rowCount = rs.getLong(1);
                    if(rowCount > Integer.MAX_VALUE)
                        throw new DAOException("Category count exceeds integer range: " + rowCount, null);
                    return (int) rowCount;
                } else {
                    return 0;
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to get category count.", e);
        }
    }
}
