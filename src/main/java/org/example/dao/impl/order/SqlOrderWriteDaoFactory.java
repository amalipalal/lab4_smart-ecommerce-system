package org.example.dao.impl.order;

import org.example.dao.interfaces.order.OrderWriteDao;
import org.example.dao.interfaces.order.OrderWriteDaoFactory;

import java.sql.Connection;

public class SqlOrderWriteDaoFactory implements OrderWriteDaoFactory {

    @Override
    public OrderWriteDao create(Connection connection) {
        return new SqlOrderWriteDao(connection);
    }

}
