package org.example.dto.product;

import java.util.UUID;

public record ProductResponse(
        UUID productId,
        String name,
        String description,
        double price,
        int stock
) {}
