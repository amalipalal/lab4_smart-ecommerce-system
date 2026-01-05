package org.example.dto.category;

import org.example.model.Category;

import java.time.Instant;
import java.util.UUID;

public record CreateCategoryResponse(
        UUID categoryId,
        String name,
        String description,
        Instant createdAt
) {
    public CreateCategoryResponse(Category category) {
        this(category.getCategoryId(), category.getName(), category.getDescription(), category.getCreatedAt());
    }
}
