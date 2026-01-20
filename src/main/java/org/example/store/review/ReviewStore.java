package org.example.store.review;

import org.example.application.ApplicationCache;
import org.example.config.DataSource;
import org.example.config.exception.DatabaseConnectionException;
import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.ReviewDAO;
import org.example.model.Review;
import org.example.store.review.exception.ReviewCreationException;
import org.example.store.review.exception.ReviewCountException;
import org.example.store.review.exception.ReviewRetrievalException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ReviewStore {
    private  final DataSource dataSource;
    private final ApplicationCache cache;
    private final ReviewDAO reviewDao;

    public ReviewStore(DataSource dataSource, ApplicationCache cache, ReviewDAO reviewDao) {
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
            } catch (DAOException e) {
                conn.rollback();
                throw new ReviewCreationException(review.getProductId().toString());
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
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
        } catch (DAOException e) {
            throw new ReviewRetrievalException(productId.toString());
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public int countReviewsByProduct(UUID productId) {
        try (Connection conn = dataSource.getConnection()) {
            String key = "review:count:" + productId;
            return this.cache.getOrLoad(key, () -> this.reviewDao.countByProduct(conn, productId));
        } catch (DAOException e) {
            throw new ReviewCountException(productId.toString());
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }
}
