package com.raymondpang365.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * Main class for configuring and starting the FIX acceptor.
 */
public class ExchangeSimulatorServer {
    private final static Logger logger = LoggerFactory.getLogger(ExchangeSimulatorServer.class);

    private final SocketAcceptor acceptor;
    private final FixAcceptor application;

    public ExchangeSimulatorServer() throws Exception {
        final SessionSettings settings = getSessionSettings();
        application = new FixAcceptor(settings);
        final MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        final LogFactory logFactory = new ScreenLogFactory(true, true, true);
        final MessageFactory messageFactory = new DefaultMessageFactory();
        acceptor = new SocketAcceptor(application, messageStoreFactory, settings, logFactory, messageFactory);
    }

    private SessionSettings getSessionSettings() throws IOException, ConfigError {
        try (final InputStream inputStream =
                     ExchangeSimulatorServer.class.getResourceAsStream("/fixAcceptor.properties");) {
            return new SessionSettings(inputStream);
        }
    }

    private void start() throws RuntimeError, ConfigError {
        acceptor.start();
    }

    private void stop() {
        acceptor.stop();
        application.cleanUp();
    }

    public static void main(final String[] args) {
        try {
            final ExchangeSimulatorServer exchangeSimulatorServer = new ExchangeSimulatorServer();
            exchangeSimulatorServer.start();
            logger.info("press <enter> to quit");
            System.in.read();
            exchangeSimulatorServer.stop();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
