package org.example.dto.review;

import org.example.model.Review;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID reviewId,
        UUID productId,
        UUID customerId,
        int rating,
        String comment,
        Instant createdAt
) {
    public ReviewResponse(Review review) {
        this(
                review.getReviewId(),
                review.getProductId(),
                review.getCustomerId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
