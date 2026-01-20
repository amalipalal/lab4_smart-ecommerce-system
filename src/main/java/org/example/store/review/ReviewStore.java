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

    /**
     * Persist a new {@link org.example.model.Review} inside a database transaction.
     *
     * This method delegates persistence to {@link org.example.dao.interfaces.ReviewDAO#save(java.sql.Connection, org.example.model.Review)}
     * and invalidates review-related cache keys on success.
     *
     * @param review the review to persist
     * @return the persisted {@link Review}
     * @throws org.example.store.review.exception.ReviewCreationException when persistence via the DAO fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
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

    /**
     * Load a page of reviews for the given product id.
     *
     * Results are loaded via {@link org.example.dao.interfaces.ReviewDAO#findByProduct(java.sql.Connection, java.util.UUID, int, int)}
     * and cached using {@link org.example.application.ApplicationCache#getOrLoad}.
     *
     * @param productId product identifier to fetch reviews for
     * @param limit maximum number of reviews to return
     * @param offset zero-based offset for paging
     * @return list of {@link Review} for the requested page
     * @throws org.example.store.review.exception.ReviewRetrievalException when DAO retrieval fails
     * @throws org.example.config.exception.DatabaseConnectionException when a DB connection cannot be obtained
     */
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
