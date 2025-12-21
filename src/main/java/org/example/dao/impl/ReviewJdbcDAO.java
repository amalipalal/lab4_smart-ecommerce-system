package org.example.dao.impl;

import org.example.dao.ReviewDAO;
import org.example.dao.exception.DAOException;
import org.example.model.Review;
import org.example.util.DBConnection;
import org.example.util.exception.DatabaseConnectionException;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReviewJdbcDAO implements ReviewDAO {

    private static final String FIND_BY_PRODUCT = """
        SELECT * FROM review
        WHERE product_id = ?
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?
        """;

    private static final String SAVE = """
        INSERT INTO review (
            review_id, product_id, customer_id,
            rating, comment, created_at
        )
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    private static final String DELETE = """
        DELETE FROM review
        WHERE review_id = ?
        """;

    @Override
    public List<Review> findByProduct(UUID productId, int limit, int offset)
            throws DAOException {

        List<Review> reviews = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_PRODUCT)) {

            ps.setObject(1, productId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(map(rs));
                }
            }
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to fetch reviews for product " + productId, e);
        }
        return reviews;
    }

    @Override
    public void save(Review review) throws DAOException {
        try {
            insertionQuery(SAVE, ps -> {
                ps.setObject(1, review.getReviewId());
                ps.setObject(2, review.getProductId());
                ps.setObject(3, review.getCustomerId());
                ps.setInt(4, review.getRating());
                ps.setString(5, review.getComment());
                ps.setTimestamp(6, Timestamp.from(review.getCreatedAt()));
            });
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to save review", e);
        }
    }

    @Override
    public void delete(UUID reviewId) throws DAOException {
        try {
            insertionQuery(DELETE, ps -> ps.setObject(1, reviewId));
        } catch (SQLException | DatabaseConnectionException e) {
            throw new DAOException("Failed to delete review " + reviewId, e);
        }
    }

    private Review map(ResultSet rs) throws SQLException {
        return new Review(
                rs.getObject("review_id", UUID.class),
                rs.getObject("product_id", UUID.class),
                rs.getObject("customer_id", UUID.class),
                rs.getInt("rating"),
                rs.getString("comment"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    private void insertionQuery(String sql, StatementPreparer preparer)
            throws SQLException, DatabaseConnectionException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preparer.prepare(ps);
            ps.executeUpdate();
        }
    }

    @FunctionalInterface
    private interface StatementPreparer {
        void prepare(PreparedStatement ps) throws SQLException;
    }
}
