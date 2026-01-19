package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Review;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public interface ReviewDAO {

    List<Review> findByProduct(Connection connection, UUID productId, int limit, int offset) throws DAOException;

    void save(Review review) throws DAOException;

    void save(Connection connection, Review review) throws DAOException;

    void delete(Connection connection, UUID reviewId) throws DAOException;
}
