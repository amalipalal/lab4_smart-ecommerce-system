package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Review;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public interface ReviewDAO {

    /**
     * Load a page of {@link Review} for a product.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param productId the product identifier
     * @param limit maximum number of reviews to return
     * @param offset zero-based offset for paging
     * @return list of reviews for the product
     * @throws DAOException on DAO errors
     */
    List<Review> findByProduct(Connection connection, UUID productId, int limit, int offset) throws DAOException;

    /**
     * Count reviews for a product.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param productId the product identifier
     * @return total number of reviews for the product
     * @throws DAOException on DAO errors
     */
    int countByProduct(Connection connection, UUID productId) throws DAOException;

    /**
     * Persist a new {@link Review}.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param review the review to save
     * @throws DAOException on DAO errors
     */
    void save(Connection connection, Review review) throws DAOException;

    /**
     * Delete a review by id.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param reviewId the review identifier to delete
     * @throws DAOException on DAO errors
     */
    void delete(Connection connection, UUID reviewId) throws DAOException;
}
