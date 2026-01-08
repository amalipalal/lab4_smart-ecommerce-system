package org.example.dto.category;

import org.example.model.Category;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID categoryId,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public CategoryResponse(Category category) {
        this(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
