package org.example.service;

import org.example.UnitOfWorkFactory;
import org.example.cache.ProductCache;
import org.example.dao.exception.DAOException;
import org.example.dao.interfaces.ProductWriteDaoFactory;
import org.example.dao.interfaces.product.ProductReadDao;
import org.example.dao.interfaces.product.ProductWriteDao;
import org.example.dao.interfaces.UnitOfWork;
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

    private final ProductReadDao productReadDao;
    private final ProductCache cache;
    private final ProductWriteDaoFactory productWriteDaoFactory;
    private final UnitOfWorkFactory factory;

    public ProductService(ProductReadDao productReadDao,
                          ProductCache cache,
                          ProductWriteDaoFactory productWriteDaoFactory,
                          UnitOfWorkFactory factory) {
        this.productReadDao = productReadDao;
        this.cache = cache;
        this.productWriteDaoFactory = productWriteDaoFactory;
        this.factory = factory;
    }

    public CreateProductResponse createProduct(CreateProductRequest request) {
        UnitOfWork unitOfWork = this.factory.create();
        try {
            ProductWriteDao productDao = instantiateProductWriteDao(unitOfWork);

            var product = new Product(UUID.randomUUID(), request.name(), request.description(),
                    request.price(), request.stock(), request.categoryId(), Instant.now(), Instant.now());
            productDao.save(product);

            unitOfWork.commit();
            this.cache.invalidateByPrefix("product:");

            return new CreateProductResponse(product.getProductId(), product.getName(),
                    product.getDescription(), product.getCreatedAt().toString());
        } catch (Exception e) {
            unitOfWork.rollback();
            throw e;
        } finally {
            unitOfWork.close();
        }
    }

    private ProductWriteDao instantiateProductWriteDao(UnitOfWork unitOfWork) {
        return this.productWriteDaoFactory.create(unitOfWork.getConnection());
    }

    public List<ProductResponse> getAllProducts(int limit, int offset) {
        String key = "product:all:" + limit + ":" + offset;
        List<Product> products = this.cache.getOrLoad(key, () -> this.productReadDao.findAll(limit, offset));
        return products.stream().map(ProductResponse::new).toList();
    }

    public int getProductCount() {
        return this.productReadDao.countAll();
    }

    public ProductResponse getProduct(UUID productId) {
        String key = "product:" + productId.toString();
        var product = this.cache.getOrLoad(key, () -> this.productReadDao.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString())));

        return new ProductResponse(product);
    }

    public int countProductsByFilter(ProductFilter filter) {
        String key = "product:count:" + filter.hashCode();
        return this.cache.getOrLoad(key, () -> this.productReadDao.countFiltered(filter));
    }

    public void deleteProduct(UUID productId) {
        UnitOfWork unitOfWork = this.factory.create();
        try {
            ProductWriteDao productWriteDao = instantiateProductWriteDao(unitOfWork);

            productWriteDao.deleteById(productId);
            unitOfWork.commit();

            this.cache.invalidateByPrefix("product:");
        } catch (DAOException e) {
            unitOfWork.rollback();
            throw new RuntimeException(e);
        } finally {
            unitOfWork.close();
        }
    }

    public List<ProductResponse> searchProducts(ProductFilter filter, int limit, int offset) {
        String key = "product:search" + filter.hashCode() + limit + offset;
        var products = this.cache.getOrLoad(key, () -> this.productReadDao.findFiltered(filter, limit, offset));
        return products.stream().map(ProductResponse::new).toList();
    }

    public void updateProduct(UUID productId, UpdateProductRequest request) {
        UnitOfWork unitOfWork = this.factory.create();
        try {
            String key = "product:" + productId.toString();
            ProductWriteDao productDao = instantiateProductWriteDao(unitOfWork);
            var productToUpdate = this.cache.getOrLoad(key, () -> this.productReadDao.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId.toString())));

            updateFetchedProduct(request, productToUpdate);

            productDao.update(productToUpdate);
            unitOfWork.commit();
            this.cache.invalidateByPrefix("product:");
        } catch (Exception e) {
            unitOfWork.rollback();
            throw e;
        } finally {
            unitOfWork.close();
        }
    }

    private void updateFetchedProduct(UpdateProductRequest request, Product productToUpdate) {
        if(request.name() != null) productToUpdate.setName(request.name());
        if(request.description() != null) productToUpdate.setDescription(request.description());
        if(request.stock() != null) productToUpdate.setStockQuantity(request.stock());
        if(request.price() != null) productToUpdate.setPrice(request.price());
        if(request.categoryId() != null) productToUpdate.setCategoryId(request.categoryId());

        productToUpdate.setUpdatedAt(Instant.now());
    }
}
