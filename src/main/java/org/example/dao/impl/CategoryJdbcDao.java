package org.example.dao.impl;

import org.example.dao.interfaces.CategoryDao;
import org.example.dao.interfaces.StatementPreparer;
import org.example.dao.exception.DAOException;
import org.example.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryJdbcDao implements CategoryDao {

    private static  final String FIND_BY_ID = """
            SELECT category_id, name, description, created_at, updated_at
            FROM category
            WHERE category_id = ?
            """;

    private static final String FIND_BY_NAME = """
            SELECT category_id, name, description, created_at, updated_at
            FROM category
            WHERE name = ?
            """;

    private static final String SEARCH_BY_NAME = """
            SELECT category_id, name, description, created_at, updated_at
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
            SELECT category_id, name, description, created_at, updated_at
            FROM category
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
    public Optional<Category> findById(Connection conn, UUID categoryId) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {
            ps.setObject(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
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
    public Optional<Category> findByName(Connection conn, String name) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(FIND_BY_NAME)) {
            ps.setObject(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to fetch category " + name, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Category> searchByName(Connection conn, String query, int limit, int offset) throws DAOException {
        List<Category> categories = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SEARCH_BY_NAME)) {
            ps.setString(1, "%" + query + "%");
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to search categories by name", e);
        }
        return categories;
    }

    @Override
    public int countByName(Connection conn, String query) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(COUNT_BY_NAME)) {
            ps.setString(1, "%" + query + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to count categories by name", e);
        }
    }

    @Override
    public List<Category> findAll(Connection conn, int limit, int offset) throws DAOException {
        List<Category> categories = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(FIND_ALL)) {
            ps.setObject(1, limit);
            ps.setObject(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to fetch all categories" + e.getMessage(), e);
        }
        return categories;
    }

    @Override
    public void save(Connection conn, Category category) throws DAOException {
        try {
            insertionQuery(conn, SAVE, ps -> {
                ps.setObject(1, category.getCategoryId());
                ps.setString(2, category.getName());
                ps.setString(3, category.getDescription());
                ps.setTimestamp(4, Timestamp.from(category.getCreatedAt()));
                ps.setTimestamp(5, Timestamp.from(category.getUpdatedAt()));
            });
        } catch (SQLException e) {
            throw new DAOException("Failed to save " + category.getName() + "category.", e);
        }
    }

    private void insertionQuery(Connection conn, String query, StatementPreparer preparer) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            if (preparer != null) preparer.prepare(ps);
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Connection conn, Category category) throws DAOException {
        try {
            insertionQuery(conn, UPDATE, ps -> {
                ps.setString(1, category.getName());
                ps.setString(2, category.getDescription());
                ps.setTimestamp(3, Timestamp.from(category.getUpdatedAt()));
                ps.setObject(4, category.getCategoryId());
            });
        } catch (SQLException e) {
            throw new DAOException("Failed to update " + category.getName() + "category.", e);
        }
    }

    @Override
    public int count(Connection conn) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(COUNT)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long rowCount = rs.getLong(1);
                    if (rowCount > Integer.MAX_VALUE)
                        throw new DAOException("Category count exceeds integer range: " + rowCount, null);
                    return (int) rowCount;
                } else {
                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to get category count.", e);
        }
    }
}
