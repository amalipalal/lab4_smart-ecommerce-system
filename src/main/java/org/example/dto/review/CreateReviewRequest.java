package org.example.dto.review;

import java.util.UUID;

public record CreateReviewRequest(
        UUID productId,
        String email,
        int rating,
        String comment
) {}
