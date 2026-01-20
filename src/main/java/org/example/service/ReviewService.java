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

    /**
     * Create a new review for a product.
     *
     * This method resolves the customer via {@link CustomerStore#findByEmail(String)} and
     * persists a newly created {@link Review} via {@link ReviewStore#createReview(Review)}.
     *
     * @param request the incoming {@link org.example.dto.review.CreateReviewRequest} containing
     *                product id, customer email, rating and comment
     * @return a {@link ReviewResponse} representing the saved review
     * @throws CustomerNotFoundException if no customer exists with the given email
     */
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

    /**
     * Retrieve reviews for a product page.
     *
     * Delegates to {@link ReviewStore#getReviewsByProduct(java.util.UUID, int, int)} and
     * converts results to {@link ReviewResponse}.
     *
     * @param productId the product identifier
     * @param limit     maximum number of reviews to return
     * @param offset    zero-based offset for paging
     * @return list of {@link ReviewResponse} for the requested page
     */
    public List<ReviewResponse> getProductReviews(UUID productId, int limit, int offset) {
        return this.reviewStore.getReviewsByProduct(productId, limit, offset).stream()
                .map(ReviewResponse::new)
                .toList();
    }

    public int countProductReviews(UUID productId) {
        return this.reviewStore.countReviewsByProduct(productId);
    }
}
