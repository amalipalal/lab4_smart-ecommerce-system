package org.example.dao.interfaces;

import org.example.dao.interfaces.product.ProductWriteDao;

import java.sql.Connection;

public interface ProductWriteDaoFactory {
    ProductWriteDao create (Connection connection);
}
