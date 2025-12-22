package org.example.dto.product;

import java.util.UUID;

public record CreateProductRequest(
        String name,
        String description,
        double price,
        int stock,
        UUID categoryId
) {}
