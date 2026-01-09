package org.example.dto.product;

import java.util.UUID;

public record UpdateProductRequest(
        String name,
        String description,
        Double price,
        UUID categoryId,
        Integer stock
) {}
