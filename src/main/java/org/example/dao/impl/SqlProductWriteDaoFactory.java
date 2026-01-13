package org.example.dao.impl;

import org.example.dao.impl.product.SqlProductWriteDao;
import org.example.dao.interfaces.ProductWriteDaoFactory;
import org.example.dao.interfaces.product.ProductWriteDao;

import java.sql.Connection;

public class SqlProductWriteDaoFactory implements ProductWriteDaoFactory {

    @Override
    public ProductWriteDao create(Connection connection) {
        return new SqlProductWriteDao(connection);
    }
}
