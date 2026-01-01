package org.example.dto.product;

import org.example.model.Product;

import java.util.UUID;

public record ProductResponse(
        UUID productId,
        String name,
        String description,
        double price,
        int stock
) {

    public ProductResponse(Product product) {
        this(product.getProductId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getStockQuantity());
    }
}
