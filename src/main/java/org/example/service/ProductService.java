package org.example.service;

import org.example.dto.product.CreateProductRequest;
import org.example.dto.product.CreateProductResponse;
import org.example.dto.product.ProductResponse;
import org.example.dto.product.UpdateProductRequest;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.store.ProductStore;

import java.util.List;
import java.util.UUID;

public class ProductService {

    private final ProductStore productStore;

    public ProductService(ProductStore productStore) {
        this.productStore = productStore;
    }

    public CreateProductResponse createProduct(CreateProductRequest request) {
        Product product = this.productStore.createProduct(request);
        return new CreateProductResponse(product.getProductId(), product.getName(),
                product.getDescription(), product.getCreatedAt().toString());
    }

    public List<ProductResponse> getAllProducts(int limit, int offset) {
        List<Product> products = this.productStore.getAllProducts(limit, offset);
        return products.stream().map(ProductResponse::new).toList();
    }

    public int getProductCount() {
        return this.productStore.countAllProducts();
    }

    public ProductResponse getProduct(UUID productId) {
        Product product = this.productStore.getProduct(productId);
        return new ProductResponse(product);
    }

    public int countProductsByFilter(ProductFilter filter) {
        return this.productStore.countProductsByFilter(filter);
    }

    public void deleteProduct(UUID productId) {
        this.productStore.deleteProduct(productId);
    }

    public List<ProductResponse> searchProducts(ProductFilter filter, int limit, int offset) {
        List<Product> products = this.productStore.searchProducts(filter, limit, offset);
        return products.stream().map(ProductResponse::new).toList();
    }

    public void updateProduct(UUID productId, UpdateProductRequest request) {
        this.productStore.updateProduct(productId, request);
    }
}
