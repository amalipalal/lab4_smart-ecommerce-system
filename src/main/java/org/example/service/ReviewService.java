package org.example.service;

import org.example.dto.review.CreateReviewRequest;
import org.example.dto.review.ReviewResponse;
import org.example.model.Review;
import org.example.service.exception.CustomerNotFoundException;
import org.example.store.customer.CustomerStore;
import org.example.store.review.ReviewStore;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReviewService {
    private final ReviewStore reviewStore;
    private final CustomerStore customerStore;

    public ReviewService(ReviewStore reviewStore, CustomerStore customerStore) {
        this.reviewStore = reviewStore;
        this.customerStore = customerStore;
    }

    public ReviewResponse createReview(CreateReviewRequest request) {
        var customer = customerStore.findByEmail(request.email())
                .orElseThrow(() -> new CustomerNotFoundException(request.email()));

        Review review = new Review(
                UUID.randomUUID(),
                request.productId(),
                customer.getCustomerId(),
                request.rating(),
                request.comment(),
                Instant.now()
        );

        reviewStore.createReview(review);
        return new ReviewResponse(review);
    }

    public List<ReviewResponse> getProductReviews(UUID productId, int limit, int offset) {
        return this.reviewStore.getReviewsByProduct(productId, limit, offset).stream()
                .map(ReviewResponse::new)
                .toList();
    }

    public int countProductReviews(UUID productId) {
        return this.reviewStore.countReviewsByProduct(productId);
    }
}
