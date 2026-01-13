package org.example.dao.impl;

import org.example.dao.impl.category.SqlCategoryWriteDao;
import org.example.dao.interfaces.CategoryWriteDaoFactory;
import org.example.dao.interfaces.category.CategoryWriteDao;

import java.sql.Connection;

public class SqlCategoryWriteDaoFactory implements CategoryWriteDaoFactory {

    @Override
    public CategoryWriteDao create(Connection connection) {
        return new SqlCategoryWriteDao(connection);
    }
}
