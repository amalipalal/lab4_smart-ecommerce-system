package org.example.dao.interfaces.order;

import org.example.dao.exception.DAOException;
import org.example.model.Orders;

public interface OrderWriteDao {
    void save(Orders order) throws DAOException;
}
