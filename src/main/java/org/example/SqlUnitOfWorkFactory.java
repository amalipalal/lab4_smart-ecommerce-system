package org.example;

import org.example.dao.impl.SqlUnitOfWork;
import org.example.dao.interfaces.UnitOfWork;
import org.example.util.exception.DatabaseConnectionException;

public class SqlUnitOfWorkFactory implements UnitOfWorkFactory {

    @Override
    public UnitOfWork create() throws DatabaseConnectionException {
        return new SqlUnitOfWork();
    }
}