package org.example.service;

import org.example.dao.ProductDAO;
import org.example.dao.exception.DAOException;
import org.example.dto.product.CreateProductRequest;
import org.example.dto.product.CreateProductResponse;
import org.example.dto.product.ProductResponse;
import org.example.model.Product;
import org.example.service.exception.ProductNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public CreateProductResponse createProduct(CreateProductRequest request) {
        try {
            var product = new Product(UUID.randomUUID(), request.name(), request.description(),
                    request.price(), request.stock(), request.categoryId(), Instant.now(), Instant.now());
            productDAO.save(product);

            return new CreateProductResponse(product.getProductId(), product.getName(),
                    product.getDescription(), product.getCreatedAt().toString());
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<ProductResponse> getAllProducts(int limit, int offset) {
        try {
            List<Product> products = this.productDAO.findAll(limit, offset);
            return products.stream().map(ProductResponse::new).toList();
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    public ProductResponse getProduct(UUID productId) {
        try {
            var product = this.productDAO.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
            return new ProductResponse(
                    product.getProductId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getStockQuantity()
            );
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteProduct(UUID productId) {
        try {
            this.productDAO.deleteById(productId);
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ProductResponse> searchProducts(String query, int limit, int offset) {
        try {
            List<Product> products = this.productDAO.searchByName(query, limit, offset);
            return products.stream().map(ProductResponse::new).toList();
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }
}
