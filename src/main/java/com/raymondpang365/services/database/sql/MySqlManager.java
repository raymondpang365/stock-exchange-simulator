package com.raymondpang365.services.database.sql;

import com.raymondpang365.services.database.DataManager;
import com.raymondpang365.utility.database.MySqlConnection;
import com.raymondpang365.utility.marketData.MarketData;
import com.raymondpang365.utility.order.Order;
import com.raymondpang365.utility.order.OrderSide;
import com.raymondpang365.utility.order.OrderTimeInForce;
import com.raymondpang365.utility.order.OrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.raymondpang365.utility.utils.DatabaseUtils.convertDateToLocalDateTime;
import static com.raymondpang365.utility.utils.DatabaseUtils.convertSqlDateToUtilDate;

public class MySqlManager implements DataManager {
    private static final Logger logger = LoggerFactory.getLogger(MySqlManager.class);

    private final MySqlConnection mySqlConnection;

    public MySqlManager(final MySqlConnection mySqlConnection) {
        this.mySqlConnection = mySqlConnection;
    }

    @Override
    public void storeOrder(final Order order) {
        try (final CallableStatement stm = mySqlConnection.getConnection()
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
        } catch (final Exception ex) {
            logger.warn("Failed to store order {}, due to: {}", order, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Order> getOrders(final Optional<OrderType> orderType) {
        logger.info("Starting to get orders data...");
        final List<Order> result = new ArrayList<>();
        try (final CallableStatement stm = mySqlConnection.getConnection()
                                                          .prepareCall("{call getOrders (?)}")) {
            if (orderType.isPresent()) {
                stm.setString(1, orderType.get().toString());
            } else {
                stm.setNull(1, java.sql.Types.VARCHAR);
            }
            final ResultSet rs = stm.executeQuery();
            while (rs.next())
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
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
        logger.info("Number of orders retrieved: " + result.size());
        return result;
    }

    @Override
    public void storeMarketDataItems(final List<MarketData> marketDataItems, final boolean deleteFirst) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<MarketData> getMarketData(final Optional<String> symbol) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void close() throws Exception {
        mySqlConnection.close();
    }

    public void setCredit(final String counterpartyId, final double credit) {
        try (final CallableStatement stm = mySqlConnection.getConnection()
                                                          .prepareCall("{call setCredit (?,?)}")) {
            stm.setString(1, counterpartyId);
            stm.setDouble(2, credit);
            stm.execute();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasEnoughCredit(final String counterpartyId, final double credit) {
        try (final CallableStatement stm = mySqlConnection.getConnection().prepareCall("{? = call hasEnoughCredit (?,?)}")) {
            stm.registerOutParameter(1, Types.DOUBLE);
            stm.setString(2, counterpartyId);
            stm.setDouble(3, credit);
            stm.execute();
            return stm.getBoolean(1);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
