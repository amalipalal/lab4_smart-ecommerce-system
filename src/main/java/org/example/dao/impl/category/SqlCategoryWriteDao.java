package org.example.dao.impl.category;

import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.StatementPreparer;
import org.example.dao.interfaces.category.CategoryWriteDao;
import org.example.model.Category;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SqlCategoryWriteDao implements CategoryWriteDao {

    private final Connection conn;

    public SqlCategoryWriteDao(Connection connection) {
        this.conn = connection;
    }

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
        try(PreparedStatement ps = conn.prepareStatement(query);) {
            if(preparer != null) preparer.prepare(ps);

            ps.executeUpdate();
        }
    }

    @Override
    public void update(Category category) throws DAOException {
        try{
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
}
