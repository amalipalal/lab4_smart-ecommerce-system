package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Product;
import org.example.model.ProductFilter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductReadDao {

    Optional<Product> findById(UUID productId) throws DAOException;

    List<Product> findAll(int limit, int offset) throws DAOException;

    int countAll() throws DAOException;

    List<Product> findFiltered(ProductFilter filter, int limit, int offset) throws DAOException;

    int countFiltered(ProductFilter filter) throws DAOException;
}
