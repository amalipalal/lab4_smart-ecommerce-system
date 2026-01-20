 import org.example.dto.review.CreateReviewRequest;
import org.example.dto.review.ReviewResponse;
import org.example.model.Customer;
import org.example.model.Review;
import org.example.service.ReviewService;
import org.example.service.exception.CustomerNotFoundException;
import org.example.store.customer.CustomerStore;
import org.example.store.review.ReviewStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewStore reviewStore;

    @Mock
    private CustomerStore customerStore;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("Should create review successfully and associate with resolved customer")
    void shouldCreateReviewSuccessfullyAndAssociateWithResolvedCustomer() {
        UUID productId = UUID.randomUUID();
        String email = "user@example.com";
        CreateReviewRequest request = new CreateReviewRequest(productId, email, 5, "Great product");

        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "First", "Last", email, "123", Instant.now());

        when(customerStore.findByEmail(email)).thenReturn(Optional.of(customer));
        when(reviewStore.createReview(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewService.createReview(request);

        Assertions.assertEquals(productId, response.productId());
        Assertions.assertEquals(customerId, response.customerId());
        Assertions.assertEquals(5, response.rating());
        Assertions.assertEquals("Great product", response.comment());
        verify(customerStore).findByEmail(email);
        verify(reviewStore).createReview(argThat(r ->
                r.getProductId().equals(productId) &&
                r.getCustomerId().equals(customerId) &&
                r.getRating() == 5 &&
                "Great product".equals(r.getComment())
        ));
    }

    @Test
    @DisplayName("Should throw when creating review for unknown customer")
    void shouldThrowWhenCreatingReviewForUnknownCustomer() {
        UUID productId = UUID.randomUUID();
        String email = "missing@example.com";
        CreateReviewRequest request = new CreateReviewRequest(productId, email, 4, "Nice");

        when(customerStore.findByEmail(email)).thenReturn(Optional.empty());

        Assertions.assertThrows(CustomerNotFoundException.class, () -> reviewService.createReview(request));

        verify(customerStore).findByEmail(email);
        verify(reviewStore, never()).createReview(any());
    }

    @Test
    @DisplayName("Should return empty list when no reviews exist for product")
    void shouldReturnEmptyListWhenNoReviewsExistForProduct() {
        UUID productId = UUID.randomUUID();

        when(reviewStore.getReviewsByProduct(productId, 10, 0)).thenReturn(List.of());

        List<ReviewResponse> result = reviewService.getProductReviews(productId, 10, 0);

        Assertions.assertEquals(0, result.size());
        verify(reviewStore).getReviewsByProduct(productId, 10, 0);
    }

    @Test
    @DisplayName("Should get product reviews and map to response")
    void shouldGetProductReviewsAndMapToResponse() {
        UUID productId = UUID.randomUUID();
        Review r1 = new Review(UUID.randomUUID(), productId, UUID.randomUUID(), 5, "Excellent", Instant.now());
        Review r2 = new Review(UUID.randomUUID(), productId, UUID.randomUUID(), 3, "Okay", Instant.now());

        when(reviewStore.getReviewsByProduct(productId, 5, 0)).thenReturn(List.of(r1, r2));

        List<ReviewResponse> result = reviewService.getProductReviews(productId, 5, 0);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(5, result.get(0).rating());
        Assertions.assertEquals("Okay", result.get(1).comment());
        verify(reviewStore).getReviewsByProduct(productId, 5, 0);
    }

    @Test
    @DisplayName("Should count reviews for a product")
    void shouldCountReviewsForProduct() {
        UUID productId = UUID.randomUUID();

        when(reviewStore.countReviewsByProduct(productId)).thenReturn(7);

        int count = reviewService.countProductReviews(productId);

        Assertions.assertEquals(7, count);
        verify(reviewStore).countReviewsByProduct(productId);
    }

}
