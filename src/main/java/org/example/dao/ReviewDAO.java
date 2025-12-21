package org.example.dao;

import org.example.dao.exception.DAOException;
import org.example.model.Review;

import java.util.List;
import java.util.UUID;

public interface ReviewDAO {

    List<Review> findByProduct(UUID productId, int limit, int offset) throws DAOException;

    void save(Review review) throws DAOException;

    void delete(UUID reviewId) throws DAOException;
}
