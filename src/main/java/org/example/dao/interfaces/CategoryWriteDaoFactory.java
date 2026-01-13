package org.example.dao.interfaces;

import org.example.dao.interfaces.category.CategoryWriteDao;

import java.sql.Connection;

public interface CategoryWriteDaoFactory {
    CategoryWriteDao create (Connection connection);
}
