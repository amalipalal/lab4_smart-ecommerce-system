package org.example.persistence;

import org.example.util.exception.DatabaseConnectionException;

public interface UnitOfWorkFactory {
    UnitOfWork create() throws DatabaseConnectionException;
}
