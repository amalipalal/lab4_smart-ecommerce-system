package org.example.dao.interfaces.order;

import java.sql.Connection;

public interface OrderWriteDaoFactory {
    OrderWriteDao create(Connection connection);
}
