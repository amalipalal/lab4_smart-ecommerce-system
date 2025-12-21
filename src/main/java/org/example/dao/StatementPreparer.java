package org.example.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementPreparer {
    void prepare(PreparedStatement ps) throws SQLException;
}
