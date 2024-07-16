package com.raymondpang365.services.simulation.marketData;

import com.raymondpang365.services.database.noSql.MongoDBConnection;
import com.raymondpang365.services.database.noSql.MongoDBManager;
import com.raymondpang365.utility.DestinationType;
import com.raymondpang365.utility.ExchangeSimulatorMessageProducer;
import com.raymondpang365.utility.database.DatabaseProperties;
import com.raymondpang365.utility.marketData.MarketData;
import com.raymondpang365.utility.utils.MarketDataUtils;
import com.raymondpang365.utility.utils.SimulatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Randomly builds market data items, publishes them to activeMQ.marketDataTopic and stores them into mongoDB
 * marketDataCollection.
 */
public class MarketDataProducer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MarketDataProducer.class);

    private final ExchangeSimulatorMessageProducer marketDataProducer;
    private final MongoDBManager mongoDBManager;
    private final ExecutorService executorService;
    private final Properties properties;

    public MarketDataProducer(final Properties properties) throws Exception {
        this.properties = properties;
        marketDataProducer
                = new ExchangeSimulatorMessageProducer(properties.getProperty("activeMQ.url"),
                                                       properties.getProperty("activeMQ.marketDataTopic"),
                                                       DestinationType.Topic,
                                                       "MarketDataProducer",
                                                       null);
        marketDataProducer.start();

        mongoDBManager = new MongoDBManager(
                new MongoDBConnection(
                        new DatabaseProperties(properties.getProperty("mongoDB.host"),
                                               Integer.parseInt(properties.getProperty("mongoDB.port")),
                                               properties.getProperty("mongoDB.database"))),
                properties.getProperty("mongoDB.executedOrdersCollection"),
                properties.getProperty("mongoDB.marketDataCollection"));

        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void run() {
        final List<String> allowedSymbols = Arrays.stream(properties.getProperty("allowedSymbols")
                                                                    .split(","))
                                                  .collect(Collectors.toList());

        while (!Thread.currentThread().isInterrupted()) {
            final ArrayList<MarketData> marketDataItems = new ArrayList<>(allowedSymbols.size());
            allowedSymbols.forEach(symbol -> marketDataItems.add(MarketDataUtils.buildRandomMarketDataTick(symbol)));

            try {
                marketDataProducer.getProducer()
                                  .send(marketDataProducer.getSession()
                                                          .createObjectMessage(marketDataItems));
                executorService.execute(() -> mongoDBManager.storeMarketDataItems(marketDataItems, false));
                TimeUnit.SECONDS.sleep(Integer.parseInt(properties.getProperty("marketDataPublishingPeriod")));
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final Exception e) {
                logger.warn("Unable to produce marked data, due to: {}", e.getMessage());
            }
        }
        cleanUp();
    }

    private void cleanUp() {
        try {
            marketDataProducer.stop();
            mongoDBManager.close();
            SimulatorUtils.shutdownExecutorService(executorService, 1, TimeUnit.SECONDS);
        } catch (final Exception ex) {
            logger.warn(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static void main(final String[] args) throws Exception {
        final ExecutorService es = Executors.newSingleThreadExecutor();
        final Future<?> f = es.submit(new MarketDataProducer(
                SimulatorUtils.getApplicationProperties("stockExchangeSimulator.properties")));
        TimeUnit.SECONDS.sleep(10L);
        f.cancel(true);
        SimulatorUtils.shutdownExecutorService(es, 1L, TimeUnit.SECONDS);
    }
}
