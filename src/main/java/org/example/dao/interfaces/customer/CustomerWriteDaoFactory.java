package org.example.dao.interfaces.customer;

import java.sql.Connection;

public interface CustomerWriteDaoFactory {
    CustomerWriteDao create(Connection connection);
}
