package org.example.store;

import org.example.cache.ProductCache;
import org.example.config.DataSource;
import org.example.dao.interfaces.ReviewDAO;
import org.example.model.Review;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ReviewStore {
    private  final DataSource dataSource;
    private final ProductCache cache;
    private final ReviewDAO reviewDao;

    public ReviewStore(DataSource dataSource, ProductCache cache, ReviewDAO reviewDao) {
        this.dataSource = dataSource;
        this.cache = cache;
        this.reviewDao = reviewDao;
    }

    public Review createReview(Review review) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                this.reviewDao.save(conn, review);
                conn.commit();
                invalidateReviewCache(review.getProductId());
                return review;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to create review.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    private void invalidateReviewCache(UUID productId) {
        this.cache.invalidateByPrefix("review:product:" + productId);
        this.cache.invalidateByPrefix("review:count:" + productId);
    }

    public List<Review> getReviewsByProduct(UUID productId, int limit, int offset) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "review:product:" + productId + ":" + limit + ":" + offset;
            return this.cache.getOrLoad(key, () -> this.reviewDao.findByProduct(conn, productId, limit, offset));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    public int countReviewsByProduct(UUID productId) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "review:count:" + productId;
            return this.cache.getOrLoad(key, () -> this.reviewDao.countByProduct(conn, productId));
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }
}
