package com.raymondpang365.utility.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.ApplVerID;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class SimulatorUtils {

    private static final Logger logger = LoggerFactory.getLogger(SimulatorUtils.class);

    public static final String lineSeparator = System.getProperty("line.separator");
    public static final String lineSeparatorAndTab = lineSeparator + "\t";

    private SimulatorUtils() {
    }

    public static Properties getApplicationProperties(final String propertiesFileName) throws IOException {
        final Properties p = new Properties();
        try (final InputStream inputStream = ClassLoader.getSystemResourceAsStream(propertiesFileName)) {
            p.load(inputStream);
            return p;
        }
    }

    public static void shutdownExecutorService(final ExecutorService es, final long timeout, final TimeUnit timeUnit) {
        es.shutdown();
        try {
            if (!es.awaitTermination(timeout, timeUnit)) {
                es.shutdownNow();
            }
        } catch (final InterruptedException e) {
            es.shutdownNow();
        }
    }

    public static void sendMessage(final SessionID sessionID, final Message message) {
        try {
            final Session session = Session.lookupSession(sessionID);
            if (session == null) {
                throw new SessionNotFound(sessionID.toString());
            }
            final DataDictionaryProvider dataDictionaryProvider = session.getDataDictionaryProvider();
            try {
                dataDictionaryProvider.getApplicationDataDictionary(getApplVerID(session)).validate(message, true);
            } catch (final Exception e) {
                LogUtil.logThrowable(sessionID, "Outgoing message failed validation: " + e.getMessage(), e);
                return;
            }
            session.send(message); //thread safe.
        } catch (final SessionNotFound e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static ApplVerID getApplVerID(final Session session) {
        final String beginString = session.getSessionID().getBeginString();
        if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
            return new ApplVerID(ApplVerID.FIX50);
        } else {
            return MessageUtils.toApplVerID(beginString);
        }
    }
}
