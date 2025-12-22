package org.example.dto.product;

import java.util.UUID;

public record CreateProductResponse(
        UUID productId,
        String name,
        String description,
        String createdAt
) {}
