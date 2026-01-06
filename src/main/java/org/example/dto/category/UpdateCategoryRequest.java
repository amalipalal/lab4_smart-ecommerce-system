package org.example.dto.category;

import java.util.UUID;

public record UpdateCategoryRequest (
        UUID categoryId,
        String name,
        String description
) {}
