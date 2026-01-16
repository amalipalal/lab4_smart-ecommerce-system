package org.example.service;

import org.example.dto.product.CreateProductRequest;
import org.example.dto.product.CreateProductResponse;
import org.example.dto.product.ProductResponse;
import org.example.dto.product.UpdateProductRequest;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.service.exception.ProductNotFoundException;
import org.example.store.ProductStore;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ProductService {

    private final ProductStore productStore;

    public ProductService(ProductStore productStore) {
        this.productStore = productStore;
    }

    public CreateProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product(
                UUID.randomUUID(),
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.categoryId(),
                Instant.now(),
                Instant.now()
        );
        Product saved = this.productStore.createProduct(product);
        return new CreateProductResponse(saved.getProductId(), saved.getName(),
                saved.getDescription(), saved.getCreatedAt().toString());
    }

    public ProductResponse getProduct(UUID productId) {
        Product product = this.productStore.getProduct(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
        return new ProductResponse(product);
    }

    public int countProductsByFilter(ProductFilter filter) {
        return this.productStore.countProductsByFilter(filter);
    }

    public void deleteProduct(UUID productId) {
        Product existing = this.productStore.getProduct(productId).orElseThrow(
                () -> new ProductNotFoundException(productId.toString()));
        this.productStore.deleteProduct(existing.getProductId());
    }

    public List<ProductResponse> searchProducts(ProductFilter filter, int limit, int offset) {
        List<Product> products = this.productStore.searchProducts(filter, limit, offset);
        return products.stream().map(ProductResponse::new).toList();
    }

    public void updateProduct(UUID productId, UpdateProductRequest request) {
        Product existing = this.productStore.getProduct(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString()));

        Product updated = new Product(
                existing.getProductId(),
                request.name() != null ? request.name() : existing.getName(),
                request.description() != null ? request.description() : existing.getDescription(),
                request.price() != null ? request.price() : existing.getPrice(),
                request.stock() != null ? request.stock() : existing.getStockQuantity(),
                request.categoryId() != null ? request.categoryId() : existing.getCategoryId(),
                existing.getCreatedAt(),
                Instant.now()
        );

        this.productStore.updateProduct(updated);
    }
}
