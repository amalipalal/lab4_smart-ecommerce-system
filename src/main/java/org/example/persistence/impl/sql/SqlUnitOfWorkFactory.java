package org.example.persistence.impl.sql;

import org.example.persistence.UnitOfWork;
import org.example.persistence.UnitOfWorkFactory;
import org.example.util.exception.DatabaseConnectionException;

public class SqlUnitOfWorkFactory implements UnitOfWorkFactory {

    @Override
    public UnitOfWork create() throws DatabaseConnectionException {
        return new SqlUnitOfWork();
    }
}