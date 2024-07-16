package com.raymondpang365.services.simulation.orders;

import com.raymondpang365.utility.utils.SimulatorUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class OrdersProducerTest {

    @Test
    @Disabled
    void testOrdersProducer() throws Exception {
        final ExecutorService es = Executors.newSingleThreadExecutor();
        final Future<?> f = es.submit(new OrdersProducer(
                SimulatorUtils.getApplicationProperties("stockExchangeSimulator.properties")));
        TimeUnit.SECONDS.sleep(10L);
        f.cancel(true);
        SimulatorUtils.shutdownExecutorService(es, 1L, TimeUnit.SECONDS);
    }
}
