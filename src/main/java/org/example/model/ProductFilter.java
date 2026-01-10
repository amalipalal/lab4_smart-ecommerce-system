package org.example.model;

import java.util.UUID;

public record ProductFilter(
        String name,
        UUID categoryId
) {
    public boolean hasName() {
        return this.name != null;
    }

    public boolean hasCategoryId() {
        return this.categoryId != null;
    }
}
