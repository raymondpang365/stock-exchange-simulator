package com.raymondpang365.server;

import com.raymondpang365.utility.database.creditCheck.CreditCheck;
import com.raymondpang365.utility.database.creditCheck.ICreditCheck;
import com.raymondpang365.utility.marketData.MarketData;
import com.raymondpang365.utility.utils.SimulatorUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.FieldNotFound;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.*;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Given an order and a link to the market data manager, it tries to match limit and stop orders based on the current market data.
 * It always fills market orders unless they are FOK and no immediate fill is possible.
 * The market data provides bid, ask prices and sizes for a given symbol.
 */
public final class MatchingEngine implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MatchingEngine.class);
    private static final AtomicInteger orderIdSequence = new AtomicInteger(0);
    private static final AtomicInteger execIdSequence = new AtomicInteger(0);

    private final MarketDataManager marketDataManager;
    private final quickfix.fix50.NewOrderSingle order;
    private final SessionID sessionID;
    private final ICreditCheck creditCheck;

    public MatchingEngine(final BasicDataSource creditCheckConnectionPool,
                          final MarketDataManager marketDataManager,
                          final quickfix.fix50.NewOrderSingle order,
                          final SessionID sessionID) throws NumberFormatException, SQLException {
        this.marketDataManager = marketDataManager;
        this.order = order;
        this.sessionID = sessionID;
        creditCheck = new CreditCheck(creditCheckConnectionPool.getConnection());
    }

    @Override
    public void run() {
        try {
            final quickfix.fix50.ExecutionReport accept
                    = new quickfix.fix50.ExecutionReport(buildOrderID(),
                                                         buildExecID(),
                                                         new ExecType(ExecType.FILL),
                                                         new OrdStatus(OrdStatus.NEW),
                                                         order.getSide(),
                                                         new LeavesQty(order.getOrderQty().getValue()),
                                                         new CumQty(0));
            accept.set(order.getClOrdID());
            accept.set(order.getSymbol());
            SimulatorUtils.sendMessage(sessionID, accept);
            // try to fill now.
            final PriceQuantity priceQuantity = findPriceAndQuantity(order, 100);
            if (priceQuantity != null) {
                if (!creditCheck.hasEnoughCredit(priceQuantity.getValue())) {
                    sendReject(order, sessionID, true);
                }
                else {
                    final quickfix.fix50.ExecutionReport executionReport
                            = new quickfix.fix50.ExecutionReport(
                            buildOrderID(),
                            buildExecID(),
                            new ExecType(ExecType.FILL),
                            new OrdStatus(OrdStatus.FILLED),
                            order.getSide(),
                            new LeavesQty(0),
                            new CumQty(priceQuantity.getQuantity()));
                    executionReport.set(order.getClOrdID());
                    executionReport.set(order.getSymbol());
                    executionReport.set(order.getOrderQty());
                    executionReport.set(new Text(String.valueOf(priceQuantity.getMarketDataId())));
                    executionReport.set(new LastQty(priceQuantity.getQuantity()));
                    executionReport.set(new LastPx(priceQuantity.getPrice()));
                    executionReport.set(new AvgPx(priceQuantity.getPrice()));
                    creditCheck.setCredit(-priceQuantity.getValue());
                    SimulatorUtils.sendMessage(sessionID, executionReport);
                }
            } else {// order rejected.
                sendReject(order, sessionID, false);
            }
        } catch (final Exception e) {
            LogUtil.logThrowable(sessionID, e.getMessage(), e);
        } finally {
            try {
                creditCheck.closeConnection();
            } catch (final SQLException e) {
                log.warn("Unable to close credit check database connection: {}", e.getMessage());
            }
        }
    }

    private PriceQuantity findPriceAndQuantity(final quickfix.fix50.NewOrderSingle order,
                                               final int maxNrTrials) throws FieldNotFound, InterruptedException {
        int counter = 0;
        final int orderQuantity = (int) (order.getOrderQty().getValue());
        switch (order.getChar(OrdType.FIELD)) {
            case OrdType.LIMIT:
                final double limitPrice = order.getDouble(Price.FIELD);
                //loop until the limit order price is executable.
                final char limitOrderSide = order.getChar(Side.FIELD);
                while (counter < maxNrTrials) {
                    final PriceQuantity marketPriceQuantity = getMarketPriceQuantity(order);
                    if (((limitOrderSide == Side.BUY
                            && Double.compare(marketPriceQuantity.getPrice(), limitPrice) <= 0)
                            || (limitOrderSide == Side.SELL
                            && Double.compare(marketPriceQuantity.getPrice(), limitPrice) >= 0))
                            && (orderQuantity >= marketPriceQuantity.getQuantity())) {
                        log.info("Found filling price/quantity for limit order, market price: {}, limit price: {}, quantity: {}",
                                 marketPriceQuantity.getPrice(), limitPrice, orderQuantity);
                        return new PriceQuantity(marketPriceQuantity.getPrice(),
                                                 orderQuantity,
                                                 marketPriceQuantity.getMarketDataId());
                    } else {
                        if (order.getChar(TimeInForce.FIELD) == TimeInForce.FILL_OR_KILL)
                            return null;
                        log.debug("Looping to find filling price for limit order, market price: {}, limit price: {}",
                                  marketPriceQuantity.getQuantity(), limitPrice);
                        TimeUnit.MILLISECONDS.sleep(500L);
                        counter++;
                    }
                }
                return null; // price/quantity isn't found.
            default:
                final PriceQuantity marketPriceQuantity = getMarketPriceQuantity(order);
                if (marketPriceQuantity.getQuantity() < orderQuantity
                        && order.getChar(TimeInForce.FIELD) == TimeInForce.FILL_OR_KILL)
                    return null;
                return new PriceQuantity(marketPriceQuantity.getPrice(),
                                         orderQuantity,
                                         marketPriceQuantity.getMarketDataId());
        }
    }

    private PriceQuantity getMarketPriceQuantity(final Message message) throws FieldNotFound {
        final MarketData marketData = marketDataManager.get(message.getString(Symbol.FIELD));
        switch (message.getChar(Side.FIELD)) {
            case Side.BUY:
                return new PriceQuantity(marketData.getAsk(), marketData.getAskSize(), marketData.getId());
            case Side.SELL:
                return new PriceQuantity(marketData.getBid(), marketData.getBidSize(), marketData.getId());
            default:
                throw new RuntimeException(String.format("Invalid order side: %s", message.getChar(Side.FIELD)));
        }
    }

    private static void sendReject(final quickfix.fix50.NewOrderSingle order,
                                   final SessionID sessionID,
                                   final boolean creditCheckFailed) throws FieldNotFound {
        final quickfix.fix50.ExecutionReport executionReport
                = new quickfix.fix50.ExecutionReport(
                buildOrderID(),
                buildExecID(),
                new ExecType(ExecType.REJECTED),
                new OrdStatus(OrdStatus.REJECTED),
                order.getSide(),
                new LeavesQty(order.getOrderQty().getValue()),
                new CumQty(0));
        executionReport.set(order.getClOrdID());
        if (creditCheckFailed) {
            executionReport.set(new Account("Failed Credit Check"));
        }
        SimulatorUtils.sendMessage(sessionID, executionReport);
    }

    private static OrderID buildOrderID() {
        return new OrderID(String.valueOf(orderIdSequence.incrementAndGet()));
    }

    private static ExecID buildExecID() {
        return new ExecID(String.valueOf(execIdSequence.incrementAndGet()));
    }

    private static class PriceQuantity {
        private final double price;
        private final double quantity;
        private final String marketDataId;

        public PriceQuantity(final double price, final double quantity, final String marketDataId) {
            this.price = price;
            this.quantity = quantity;
            this.marketDataId = marketDataId;
        }

        public double getPrice() {
            return price;
        }

        public double getQuantity() {
            return quantity;
        }

        public String getMarketDataId() {
            return marketDataId;
        }

        public double getValue() {
            return price * quantity;
        }

    }
}
