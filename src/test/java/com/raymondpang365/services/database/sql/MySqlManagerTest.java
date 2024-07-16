package com.raymondpang365.services.database.sql;

import com.raymondpang365.utility.database.DatabaseProperties;
import com.raymondpang365.utility.order.Order;
import com.raymondpang365.utility.order.OrderSide;
import com.raymondpang365.utility.order.OrderTimeInForce;
import com.raymondpang365.utility.order.OrderType;
import com.raymondpang365.utility.utils.DatabaseUtils;
import com.raymondpang365.utility.utils.RandomOrdersBuilder;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static com.raymondpang365.utility.utils.DatabaseUtils.convertDateToLocalDateTime;
import static com.raymondpang365.utility.utils.DatabaseUtils.convertSqlDateToUtilDate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MySqlManagerTest {

    @ParameterizedTest
    @CsvSource({
            "localhost, 3306, TradingMachine, devuser, Atging@123"
    })
    @DisplayName("Test getOrders Stored Procedure using MysqlDataSource")
    @Disabled
    void testGetOrdersUsingMysqlDataSource(final String host,
                                           final int port,
                                           final String databaseName,
                                           final String userName,
                                           final String password) throws SQLException {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName, userName, password);
        assertNotNull(databaseProperties);
        final List<Order> result = new ArrayList<>();
        final MysqlDataSource creditCheckConnectionPool
                = DatabaseUtils.getMysqlDataSource(databaseProperties);
        try (final CallableStatement stm = creditCheckConnectionPool.getConnection()
                                                                    .prepareCall("{call getOrders (?)}")) {
            stm.setNull(1, java.sql.Types.VARCHAR);
            final ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                result.add(new Order(rs.getString("id"),
                                     rs.getString("symbol"),
                                     rs.getInt("quantity"),
                                     OrderSide.fromString(rs.getString("side")),
                                     OrderType.fromString(rs.getString("type")),
                                     OrderTimeInForce.fromString(rs.getString("time_in_force")),
                                     rs.getDouble("limit_price"),
                                     rs.getDouble("price"),
                                     rs.getString("original_id"),
                                     convertDateToLocalDateTime(
                                             convertSqlDateToUtilDate(rs.getDate("fill_date"))),
                                     rs.getString("rejected").equals("Y"),
                                     rs.getString("credit_check_failed").equals("Y")));
            }
        }
        assertFalse(result.isEmpty());
        result.forEach(System.out::println);
    }

    @ParameterizedTest
    @CsvSource({
            "localhost, 3306, TradingMachine, devuser, Atging@123"
    })
    @DisplayName("Test getOrders Stored Procedure using BasicDataSource")
    @Disabled
    void testGetOrdersUsingBasicDataSource(final String host,
                                           final int port,
                                           final String databaseName,
                                           final String userName,
                                           final String password) throws SQLException {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName, userName, password);
        assertNotNull(databaseProperties);
        final List<Order> result = new ArrayList<>();
        try (final BasicDataSource creditCheckConnectionPool = DatabaseUtils.getDataSource(databaseProperties, 10);
             final CallableStatement stm = creditCheckConnectionPool.getConnection()
                                                                    .prepareCall("{call getOrders (?)}")) {
            stm.setNull(1, java.sql.Types.VARCHAR);
            final ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                result.add(new Order(rs.getString("id"),
                                     rs.getString("symbol"),
                                     rs.getInt("quantity"),
                                     OrderSide.fromString(rs.getString("side")),
                                     OrderType.fromString(rs.getString("type")),
                                     OrderTimeInForce.fromString(rs.getString("time_in_force")),
                                     rs.getDouble("limit_price"),
                                     rs.getDouble("price"),
                                     rs.getString("original_id"),
                                     convertDateToLocalDateTime(
                                             convertSqlDateToUtilDate(rs.getDate("fill_date"))),
                                     rs.getString("rejected").equals("Y"),
                                     rs.getString("credit_check_failed").equals("Y")));
            }
        }
        assertFalse(result.isEmpty());
        result.forEach(System.out::println);
    }


    @ParameterizedTest
    @CsvSource({
            "localhost, 3306, TradingMachine, devuser, Atging@123"
    })
    @DisplayName("Test getOrders Stored Procedure using BasicDataSource")
    @Disabled
    void testStoreOrderUsingBasicDataSource(final String host,
                                            final int port,
                                            final String databaseName,
                                            final String userName,
                                            final String password) throws SQLException {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName, userName, password);
        assertNotNull(databaseProperties);

        final Order order = RandomOrdersBuilder.build(Collections.singletonList("RISHI"));
        assertNotNull(order);

        try (final BasicDataSource creditCheckConnectionPool = DatabaseUtils.getDataSource(databaseProperties, 10);
             final CallableStatement stm = creditCheckConnectionPool.getConnection()
                                                                    .prepareCall("{call addOrder(?,?,?,?,?,?,?,?,?,?)}")) {
            stm.setString(1, order.getId());
            stm.setString(2, order.getSymbol());
            stm.setInt(3, order.getQuantity());
            stm.setString(4, order.getSide().toString());
            stm.setString(5, order.getType().toString());
            stm.setString(6, order.getTimeInForce().toString());
            if (Objects.requireNonNull(order.getType()) == OrderType.LIMIT) {
                stm.setDouble(7, order.getLimitPrice());
            } else {
                stm.setNull(7, Types.DOUBLE);
            }
            stm.setDouble(8, order.getAvgPx());
            stm.setString(9, order.isRejected() ? "Y" : "N");
            stm.setString(10, order.isCreditCheckFailed() ? "Y" : "N");
            stm.execute();
        }
    }

}
