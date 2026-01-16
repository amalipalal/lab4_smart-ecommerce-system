package org.example.dao.impl.customer;

import org.example.dao.interfaces.customer.CustomerWriteDaoFactory;

import java.sql.Connection;

public class SqlCustomerWriteDaoFactory implements CustomerWriteDaoFactory {
    @Override
    public SqlCustomerWriteDao create(Connection connection) {
        return new SqlCustomerWriteDao(connection);
    }
}
