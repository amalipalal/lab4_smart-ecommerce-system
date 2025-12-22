package org.example.dto.category;

import java.util.UUID;

public record CreateCategoryResponse(UUID categoryId, String name, String description) {}
