package org.example.dao;

import org.example.model.Review;

import java.util.List;
import java.util.UUID;

public interface ReviewDAO {

    List<Review> findByProduct(UUID productId, int limit, int offset);

    void save(Review review);

    void delete(UUID reviewId);
}
