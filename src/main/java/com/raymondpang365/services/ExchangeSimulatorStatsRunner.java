package com.raymondpang365.services;

import com.raymondpang365.services.database.noSql.MongoDBConnection;
import com.raymondpang365.services.database.noSql.MongoDBManager;
import com.raymondpang365.utility.database.DatabaseProperties;
import com.raymondpang365.utility.order.Order;
import com.raymondpang365.utility.order.OrderSide;
import com.raymondpang365.utility.order.OrderTimeInForce;
import com.raymondpang365.utility.order.OrderType;
import com.raymondpang365.utility.utils.NumberUtils;
import com.raymondpang365.utility.utils.SimulatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.raymondpang365.utility.utils.SimulatorUtils.lineSeparator;
import static com.raymondpang365.utility.utils.SimulatorUtils.lineSeparatorAndTab;
import static java.util.stream.Collectors.groupingBy;

/**
 * Extracts statistics out of the filled orders stored in the MongoDB database
 */
public class ExchangeSimulatorStatsRunner implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeSimulatorStatsRunner.class);

    private final Properties properties;
    private final MongoDBManager mongoDBManager;

    public ExchangeSimulatorStatsRunner(final Properties properties) {
        this.properties = properties;
        mongoDBManager = new MongoDBManager(
                new MongoDBConnection(
                        new DatabaseProperties(properties.getProperty("mongoDB.host"),
                                               Integer.parseInt(properties.getProperty("mongoDB.port")),
                                               properties.getProperty("mongoDB.database"))),
                properties.getProperty("mongoDB.executedOrdersCollection"));
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final List<Order> orders = mongoDBManager.getOrders(Optional.empty());
                logger.info(groupOrdersByType(orders)
                                    + groupOrdersBySide(orders, 5)
                                    + groupOrdersBySideTypeTimeInForce(orders)
                                    + groupOrdersWithAndWithoutMarketData(orders));
                //testReduction(orders);
                TimeUnit.SECONDS.sleep(Integer.parseInt(properties.getProperty("statsPublishingPeriod")));
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final Exception e) {
                logger.warn("Unable to produce marked data, due to: " + e.getMessage());
            }
        }
        try {
            mongoDBManager.close();
        } catch (final Exception e) {
            logger.warn("Unable to close the MongoDB connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Statistics based on the order type.
     */
    private String groupOrdersByType(final List<Order> orders) {
        final StringBuilder sb = new StringBuilder(lineSeparator);
        final Map<OrderType, List<Order>> groupedOrders = orders.parallelStream()
                                                                .collect(Collectors.groupingBy(Order::getType));
        for (final Map.Entry<OrderType, List<Order>> ot : groupedOrders.entrySet()) {
            final String avgMarketPrice =
                    String.valueOf(NumberUtils.roundDouble(ot.getValue()
                                                             .stream()
                                                             .mapToDouble(Order::getAvgPx)
                                                             .average()
                                                             .getAsDouble(), 2));
            switch (ot.getKey()) {
                case MARKET:
                    sb.append("Market Orders: ")
                      .append(lineSeparatorAndTab)
                      .append("Average price: ")
                      .append(avgMarketPrice)
                      .append(lineSeparatorAndTab);
                    break;
                case LIMIT:
                    sb.append("Limit Orders: ")
                      .append(lineSeparatorAndTab)
                      .append("Average market price: ")
                      .append(avgMarketPrice)
                      .append(lineSeparatorAndTab)
                      .append("Average limit price: ")
                      .append(NumberUtils.roundDouble(ot.getValue()
                                                        .stream()
                                                        .mapToDouble(Order::getLimitPrice)
                                                        .average()
                                                        .getAsDouble(), 2))
                      .append(lineSeparatorAndTab);
                    break;
            }

            sb.append("Average quantity: ").append(NumberUtils.roundDouble(ot.getValue()
                                                                             .stream()
                                                                             .mapToDouble(Order::getQuantity)
                                                                             .average()
                                                                             .getAsDouble(), 2))
              .append(lineSeparatorAndTab);

            sb.append("Orders number: ")
              .append(ot.getValue().size())
              .append(lineSeparator)
              .append(lineSeparator);
        }
        return sb.toString();
    }

    /**
     * Statistics based on the order side.
     */
    private String groupOrdersBySide(final List<Order> orders, final int topOrdersLimit) {
        final StringBuilder sb = new StringBuilder(lineSeparator);
        final Map<OrderSide, List<Order>> groupedOrders = orders.parallelStream()
                                                                .collect(Collectors.groupingBy(Order::getSide));
        final Comparator<Order> ordersQuantityComparator = Comparator.comparingInt(Order::getQuantity);
        for (final Map.Entry<OrderSide, List<Order>> ot : groupedOrders.entrySet()) {
            switch (ot.getKey()) {
                case BUY:
                    sb.append("BUY orders number: ")
                      .append(ot.getValue().size())
                      .append(lineSeparator)
                      .append("Top ")
                      .append(topOrdersLimit)
                      .append(" biggest quantity BUY orders: ")
                      .append(lineSeparatorAndTab)
                      .append(ot.getValue()
                                .stream()
                                .sorted(ordersQuantityComparator.reversed())
                                .limit(topOrdersLimit)
                                .map(so -> so.getSymbol()
                                        + "/ " + so.getQuantity()
                                        + "/ " + so.getStoreDate())
                                .collect(Collectors.joining(lineSeparatorAndTab)))
                      .append(lineSeparator)
                      .append(lineSeparator);
                    break;
                case SELL:
                    sb.append("SELL orders number: ")
                      .append(ot.getValue().size())
                      .append(lineSeparator)
                      .append("Top ")
                      .append(topOrdersLimit)
                      .append(" smallest quantity SELL orders: ")
                      .append(lineSeparatorAndTab)
                      .append(ot.getValue()
                                .stream()
                                .sorted(Comparator.comparingInt(Order::getQuantity))
                                .limit(topOrdersLimit)
                                .map(so -> so.getSymbol()
                                        + "/ " + so.getQuantity()
                                        + "/ " + so.getStoreDate())
                                .collect(Collectors.joining(lineSeparatorAndTab)))
                      .append(lineSeparator)
                      .append(lineSeparator);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Does a three-levels grouping based on side, type and time in force
     *
     * @param orders
     * @return
     */
    private String groupOrdersBySideTypeTimeInForce(final List<Order> orders) {
        final StringBuilder sb = new StringBuilder("Orders split based on side, type and time in force.")
                .append(lineSeparator)
                .append("Number of")
                .append(lineSeparatorAndTab);
        final Map<OrderSide, Map<OrderType, Map<OrderTimeInForce, Long>>> threeLevelsGroupedOrders
                = orders.parallelStream()
                        .collect(groupingBy(Order::getSide,
                                            groupingBy(Order::getType,
                                                       groupingBy(Order::getTimeInForce,
                                                                  Collectors.counting()))));
        for (final Map.Entry<OrderSide, Map<OrderType, Map<OrderTimeInForce, Long>>> threeLeveslGroupedOrdersEntry
                : threeLevelsGroupedOrders.entrySet()) {
            for (final Map.Entry<OrderType, Map<OrderTimeInForce, Long>> groupedByOrderTypeAndTimeInForceEntry
                    : threeLeveslGroupedOrdersEntry.getValue()
                                                   .entrySet()) {
                for (final Map.Entry<OrderTimeInForce, Long> groupedByTimeInForceEntry
                        : groupedByOrderTypeAndTimeInForceEntry.getValue()
                                                               .entrySet()) {
                    sb.append(threeLeveslGroupedOrdersEntry.getKey())
                      .append("/ ")
                      .append(groupedByOrderTypeAndTimeInForceEntry.getKey())
                      .append("/ ")
                      .append(groupedByTimeInForceEntry.getKey())
                      .append(" orders: ")
                      .append(groupedByTimeInForceEntry.getValue())
                      .append(lineSeparator);
                }
            }
        }
        sb.append(lineSeparator);
        return sb.toString();
    }

    private String groupOrdersWithAndWithoutMarketData(final List<Order> orders) {
        final Map<Boolean, Long> groupByMarketDataID
                = orders.parallelStream()
                        .collect(Collectors.partitioningBy(so -> so.getMarketDataID() != null, Collectors.counting()));
        final StringBuilder sb = new StringBuilder(lineSeparator)
                .append("Orders with market data id: ")
                .append(groupByMarketDataID.get(true))
                .append(", without: ")
                .append(groupByMarketDataID.get(false))
                .append(lineSeparator);
        return sb.toString();
    }

    public static void main(final String[] args) throws Exception {
        final ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
        final Future<?> f = es.submit(new ExchangeSimulatorStatsRunner(
                SimulatorUtils.getApplicationProperties("stockExchangeSimulator.properties")));
        TimeUnit.SECONDS.sleep(60L);
        f.cancel(true);
        SimulatorUtils.shutdownExecutorService(es, 1L, TimeUnit.SECONDS);
    }
}
