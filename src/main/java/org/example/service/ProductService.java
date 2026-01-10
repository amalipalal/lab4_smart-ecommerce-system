package org.example.service;

import org.example.dao.interfaces.ProductDAO;
import org.example.dao.exception.DAOException;
import org.example.dto.product.CreateProductRequest;
import org.example.dto.product.CreateProductResponse;
import org.example.dto.product.ProductResponse;
import org.example.dto.product.UpdateProductRequest;
import org.example.model.Product;
import org.example.model.ProductFilter;
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

    public int getProductCount() {
        try {
            return this.productDAO.countAll();
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public ProductResponse getProduct(UUID productId) {
        try {
            var product = this.productDAO.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId.toString()));

            return new ProductResponse(product);
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    public int countProductsByFilter(ProductFilter filter) {
        try {
            return this.productDAO.countFiltered(filter);
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

    public List<ProductResponse> searchProducts(ProductFilter filter, int limit, int offset) {
        try {
            List<Product> products = this.productDAO.findFiltered(filter, limit, offset);
            return products.stream().map(ProductResponse::new).toList();
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateProduct(UUID productId, UpdateProductRequest request) {
        try {
            var productToUpdate = this.productDAO.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId.toString()));

            if(request.name() != null) productToUpdate.setName(request.name());
            if(request.description() != null) productToUpdate.setDescription(request.description());
            if(request.stock() != null) productToUpdate.setStockQuantity(request.stock());
            if(request.price() != null) productToUpdate.setPrice(request.price());
            if(request.categoryId() != null) productToUpdate.setCategoryId(request.categoryId());

            productToUpdate.setUpdatedAt(Instant.now());

            this.productDAO.update(productToUpdate);
        } catch (DAOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
