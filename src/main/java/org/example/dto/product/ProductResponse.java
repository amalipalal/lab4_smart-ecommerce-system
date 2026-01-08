package org.example.dto.product;

import org.example.model.Product;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID productId,
        UUID categoryId,
        String name,
        String description,
        double price,
        int stock,
        Instant updatedAt
) {

    public ProductResponse(Product product) {
        this(product.getProductId(), product.getCategoryId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getStockQuantity(), product.getUpdatedAt());
    }
}
