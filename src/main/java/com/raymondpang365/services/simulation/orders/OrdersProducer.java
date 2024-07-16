package com.raymondpang365.services.simulation.orders;

import com.raymondpang365.utility.DestinationType;
import com.raymondpang365.utility.ExchangeSimulatorMessageProducer;
import com.raymondpang365.utility.utils.RandomOrdersBuilder;
import com.raymondpang365.utility.utils.SimulatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class OrdersProducer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(OrdersProducer.class);

    private final Properties properties;
    private final ExchangeSimulatorMessageProducer ordersProducer;

    public OrdersProducer(final Properties properties) throws Exception {
        this.properties = properties;
        ordersProducer = new ExchangeSimulatorMessageProducer(properties.getProperty("activeMQ.url"),
                                                              properties.getProperty("activeMQ.ordersQueue"),
                                                              DestinationType.Queue,
                                                              "OrdersProducer",
                                                              null);
        ordersProducer.start();
    }

    @Override
    public void run() {
        final List<String> allowedSymbols = Arrays.stream(properties.getProperty("allowedSymbols")
                                                                    .split(","))
                                                  .collect(Collectors.toList());
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ordersProducer.getProducer()
                              .send(ordersProducer.getSession()
                                                  .createObjectMessage(RandomOrdersBuilder.build(allowedSymbols)));
                TimeUnit.SECONDS.sleep(Integer.parseInt(properties.getProperty("ordersPublishingPeriod")));
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final Exception e) {
                logger.warn("Failed to produce order, due to: {}", e.getMessage());
            }
        }
        //close subscription.
        try {
            ordersProducer.stop();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(final String[] args) throws Exception {
        final ExecutorService es = Executors.newSingleThreadExecutor();
        final Future<?> f = es.submit(new OrdersProducer(
                SimulatorUtils.getApplicationProperties("stockExchangeSimulator.properties")));
        TimeUnit.SECONDS.sleep(10L);
        f.cancel(true);
        SimulatorUtils.shutdownExecutorService(es, 1L, TimeUnit.SECONDS);
    }
}
