package com.raymondpang365.orderRouter;

import org.quickfixj.jmx.JmxExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Main class for configuring and starting the FIX initiator.
 */
public class OrderRouter {

    private static final Logger logger = LoggerFactory.getLogger(OrderRouter.class);
    private static final CountDownLatch monitorLatch = new CountDownLatch(1);

    private final FixInitiator myApplication;
    private final Initiator initiator;

    public OrderRouter() throws Exception {
        final SessionSettings settings = getSessionSettings();
        final MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        final LogFactory logFactory = new ScreenLogFactory(true, true, true, true);
        final MessageFactory messageFactory = new DefaultMessageFactory();

        myApplication = new FixInitiator(settings);
        initiator = new SocketInitiator(myApplication, messageStoreFactory, settings, logFactory, messageFactory);
        new JmxExporter().register(initiator);
    }

    private SessionSettings getSessionSettings() throws IOException, ConfigError {
        try (final InputStream inputStream = OrderRouter.class.getResourceAsStream("/fixInitiator.properties")) {
            return new SessionSettings(inputStream);
        }
    }

    public void start() throws RuntimeError, ConfigError {
        initiator.start();
        logger.info("Initiator started.");
    }

    public void stop() throws Exception {
        initiator.stop();
        logger.info("Initiator stopped.");
        myApplication.closeOrdersConsumer();
        monitorLatch.countDown();
    }

    public void logon() {
        initiator.getSessions()
                 .forEach(sessionId -> Session.lookupSession(sessionId).logon());
    }

    public static void main(final String[] args) throws Exception {
        final OrderRouter initiator = new OrderRouter();
        initiator.start();
        initiator.logon();
        monitorLatch.await();
    }
}
