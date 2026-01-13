package org.example.dao.interfaces.category;

import org.example.dao.exception.DAOException;
import org.example.model.Category;

public interface CategoryWriteDao {

    void save(Category category) throws DAOException;

    void update(Category category) throws DAOException;

}
