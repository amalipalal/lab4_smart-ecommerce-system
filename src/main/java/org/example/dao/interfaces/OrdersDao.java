package org.example.dao.interfaces;

import org.example.dao.exception.DAOException;
import org.example.model.Orders;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdersDao {

    /**
     * Find an order by id.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param orderId order identifier
     * @return optional order when found
     * @throws DAOException on DAO errors
     */
    Optional<Orders> findById(Connection connection, UUID orderId) throws DAOException;

    /**
     * Retrieve all orders with paging.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param limit maximum results
     * @param offset zero-based offset
     * @return list of orders
     * @throws DAOException on DAO errors
     */
    List<Orders> getAllOrders(Connection connection, int limit, int offset) throws DAOException;

    /**
     * Persist a new {@link Orders}.
     *
     * @param connection the {@link java.sql.Connection} to use
     * @param order order to save
     * @throws DAOException on DAO errors
     */
    void save(Connection connection, Orders order) throws DAOException;

    /**
     * Count all orders.
     *
     * @param conn the {@link java.sql.Connection} to use
     * @return total number of orders
     * @throws DAOException on DAO errors
     */
    int countAll(Connection conn) throws DAOException;
}
