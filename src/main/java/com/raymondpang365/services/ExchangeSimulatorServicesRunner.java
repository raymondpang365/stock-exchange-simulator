package com.raymondpang365.services;

import com.raymondpang365.services.database.OrdersBackEndStore;
import com.raymondpang365.services.simulation.marketData.MarketDataProducer;
import com.raymondpang365.services.simulation.orders.OrdersProducer;
import com.raymondpang365.utility.ExchangeSimulatorLifeCycle;
import com.raymondpang365.utility.utils.SimulatorUtils;

import javax.jms.JMSException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Starts all the support services, namely:
 *
 * <ul>
 * 	<li>MarketDataProducer: it builds random ask and bid prices for a selected range of symbols and sends them to a
 * 	queue, every X seconds.</li>
 *  <li>OrdersProducer: it builds random buy/sell market and limit orders and sends them to a queue, every X
 *  seconds.</li>
 *  <li>FilledOrdersBackEndStore: it subscribes to the FilledOrdersTopic to receive fully filled orders and stores
 *  them to MySQL and MongoDB databases.</li>
 *  <li>ExchangeSimulatorStatsRunner: prints some order execution statistics.</li>
 * </ul>
 */
public class ExchangeSimulatorServicesRunner implements ExchangeSimulatorLifeCycle {

    private final ExecutorService es;
    private final OrdersBackEndStore filledOrdersBackEndStore;
    private final Properties properties;

    private Future<?> ordersProducerFuture;
    private Future<?> marketDataProducerFuture;
    private Future<?> statsRunnerFuture;

    /**
     * Sets up the executor service with three threads for OrdersProducer, MarketDataProducer and StatsRunner.
     *
     * @param properties
     * @throws JMSException
     * @throws SQLException
     */
    public ExchangeSimulatorServicesRunner(final Properties properties) throws JMSException, SQLException {
        this.properties = properties;
        es = Executors.newFixedThreadPool(4);
        filledOrdersBackEndStore = new OrdersBackEndStore(properties);
    }


    @Override
    public void start() throws Exception {
        ordersProducerFuture = es.submit(new OrdersProducer(properties));
        marketDataProducerFuture = es.submit(new MarketDataProducer(properties));
        statsRunnerFuture = es.submit(new ExchangeSimulatorStatsRunner(properties));
        filledOrdersBackEndStore.start();
    }

    @Override
    public void stop() throws Exception {
        try {
            filledOrdersBackEndStore.stop();
            if (ordersProducerFuture != null)
                ordersProducerFuture.cancel(true);
            if (marketDataProducerFuture != null)
                marketDataProducerFuture.cancel(true);
            if (statsRunnerFuture != null)
                statsRunnerFuture.cancel(true);
        } finally {
            SimulatorUtils.shutdownExecutorService(es, 5L, TimeUnit.SECONDS);
        }
    }

    public static void main(final String[] args) throws Exception {
        final ExchangeSimulatorServicesRunner servicesRunner
                = new ExchangeSimulatorServicesRunner(
                SimulatorUtils.getApplicationProperties("stockExchangeSimulator.properties"));
        servicesRunner.start();
        // TimeUnit.SECONDS.sleep(10L);
        // servicesRunner.stop();
    }
}
