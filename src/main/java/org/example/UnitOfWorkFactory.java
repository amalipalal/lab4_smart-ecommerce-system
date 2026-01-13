package org.example;

import org.example.dao.interfaces.UnitOfWork;
import org.example.util.exception.DatabaseConnectionException;

public interface UnitOfWorkFactory {
    UnitOfWork create() throws DatabaseConnectionException;
}
