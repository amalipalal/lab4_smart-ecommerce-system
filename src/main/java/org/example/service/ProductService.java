package org.example.service;

import org.example.dto.product.CreateProductRequest;
import org.example.dto.product.CreateProductResponse;
import org.example.dto.product.ProductResponse;
import org.example.dto.product.UpdateProductRequest;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.service.exception.ProductNotFoundException;
import org.example.store.product.ProductStore;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ProductService {

    private final ProductStore productStore;

    public ProductService(ProductStore productStore) {
        this.productStore = productStore;
    }

    /**
     * Create a new product and persist it via {@link ProductStore#createProduct(org.example.model.Product)}.
     *
     * @param request the incoming {@link CreateProductRequest}
     * @return a {@link CreateProductResponse} containing identifiers and timestamps for the new product
     */
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

    /**
     * Retrieve a product by id.
     *
     * Uses {@link ProductStore#getProduct(java.util.UUID)} and throws {@link ProductNotFoundException}
     * when no product is found.
     *
     * @param productId the product identifier
     * @return a {@link ProductResponse} for the found product
     * @throws ProductNotFoundException if the product does not exist
     */
    public ProductResponse getProduct(UUID productId) {
        Product product = this.productStore.getProduct(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
        return new ProductResponse(product);
    }

    /**
     * Count products matching a filter.
     *
     * @param filter the {@link ProductFilter} criteria
     * @return number of products matching the filter
     */
    public int countProductsByFilter(ProductFilter filter) {
        return this.productStore.countProductsByFilter(filter);
    }

    /**
     * Delete a product by id.
     *
     * Ensures the product exists by calling {@link ProductStore#getProduct(java.util.UUID)} before delegating
     * to {@link ProductStore#deleteProduct(java.util.UUID)}.
     *
     * @param productId the product identifier to delete
     * @throws ProductNotFoundException if the product does not exist
     */
    public void deleteProduct(UUID productId) {
        Product existing = this.productStore.getProduct(productId).orElseThrow(
                () -> new ProductNotFoundException(productId.toString()));
        this.productStore.deleteProduct(existing.getProductId());
    }

    /**
     * Search for products using a filter with paging.
     *
     * Delegates to {@link ProductStore#searchProducts(ProductFilter, int, int)}.
     *
     * @param filter the {@link ProductFilter} to apply
     * @param limit  maximum number of results
     * @param offset zero-based offset for paging
     * @return list of {@link ProductResponse}
     */
    public List<ProductResponse> searchProducts(ProductFilter filter, int limit, int offset) {
        List<Product> products = this.productStore.searchProducts(filter, limit, offset);
        return products.stream().map(ProductResponse::new).toList();
    }

    /**
     * Update an existing product.
     *
     * Validates presence of the product via {@link ProductStore#getProduct(java.util.UUID)} and
     * delegates persistence to {@link ProductStore#updateProduct(org.example.model.Product)}.
     *
     * @param productId the product id to update
     * @param request   the incoming {@link UpdateProductRequest} with optional fields
     * @throws ProductNotFoundException if the target product does not exist
     */
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
