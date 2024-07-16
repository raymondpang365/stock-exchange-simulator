package com.raymondpang365.server;

import com.raymondpang365.utility.database.DatabaseProperties;
import com.raymondpang365.utility.utils.DatabaseUtils;
import com.raymondpang365.utility.utils.SimulatorUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.fixt11.Logon;

import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * * FIX acceptor with the following key features:
 * * <ul>
 * * 	<li>Does log-on check.</li>
 * *  <li>FIX 5.0 message cracking. Once a message is received, it then gets forwarded, in a separate thread, to a matching engine.</li>
 * *  <li>The degree of parallelism can be set by a configuration parameter.</li>
 * *  <li>Receives market data from a given queue.</li>
 * * </ul>
 */
public class FixAcceptor extends quickfix.MessageCracker implements quickfix.Application {

    private final static Logger logger = LoggerFactory.getLogger(FixAcceptor.class);

    private final MarketDataManager marketDataManager;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutorService;
    private final SessionSettings settings;
    private final BasicDataSource creditCheckConnectionPool;


    public FixAcceptor(final SessionSettings settings) throws Exception {
        this.settings = settings;
        final Properties applicationProperties
                = SimulatorUtils.getApplicationProperties("stockExchangeSimulator.properties");
        marketDataManager = new MarketDataManager(applicationProperties);
        marketDataManager.start();
        executor = Executors.newFixedThreadPool(
                Integer.parseInt(applicationProperties.getProperty("numberProcessingOrderThreads")));
        creditCheckConnectionPool
                = DatabaseUtils.getDataSource(
                new DatabaseProperties(applicationProperties.getProperty("mySQL.host"),
                                       Integer.parseInt(applicationProperties.getProperty("mySQL.port")),
                                       applicationProperties.getProperty("mySQL.database"),
                                       applicationProperties.getProperty("mySQL.userName"),
                                       applicationProperties.getProperty("mySQL.password")),
                Integer.parseInt(applicationProperties.getProperty("creditCheckDatabasePoolConnections")));
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() ->
                                                                logger.debug("Credit check database pool, idle: {}, active: {}",
                                                                             creditCheckConnectionPool.getNumIdle(),
                                                                             creditCheckConnectionPool.getNumActive()),
                                                        1, 10, TimeUnit.SECONDS);
    }

    /**
     * This method is called when quickfix creates a new session.
     * A session comes into and remains in existence for the life of the application.
     * Sessions exist whether a counterparty is connected to it.
     * As soon as a session is created, we can begin sending messages to it.
     * If no one is logged on, the messages will be sent at the time a connection is established with the counterparty.
     *
     * @param sessionId
     */
    @Override
    public void onCreate(final SessionID sessionId) {
        Session.lookupSession(sessionId)
               .getLog()
               .onEvent(String.format("Session %s created.", sessionId));
    }

    /**
     * This callback notifies us when a valid logon has been established with a counterparty.
     * This is called when a connection has been established and the FIX logon process has completed with both parties
     * exchanging valid logon messages.
     *
     * @param sessionId
     */
    @Override
    public void onLogon(final SessionID sessionId) {

    }

    /**
     * This callback notifies us when a FIX session is no longer online.
     * This could happen during a normal logout exchange or because of a forced termination or a loss of network
     * connection.
     *
     * @param sessionId
     */
    @Override
    public void onLogout(final SessionID sessionId) {

    }

    /**
     * This callback provides us with a peek at the administrative messages that are being sent from our FIX engine to
     * the counterparty.
     * This is normally not useful for an application; however, it is provided for any logging we may wish to do.
     * Notice that the FIX::Message is not const.
     * This allows us to add fields to an administrative message before it is sent out.
     *
     * @param message
     * @param sessionId
     */
    @Override
    public void toAdmin(final Message message, final SessionID sessionId) {

    }

    /**
     * This callback notifies us when an administrative message is sent from a counterparty to our FIX engine.
     * This can be useful for doing extra validation on logon messages such as for checking passwords.
     * Throwing a RejectLogon exception will disconnect the counterparty.
     *
     * @param message
     * @param sessionId
     * @throws RejectLogon
     */
    @Override
    public void fromAdmin(final Message message, final SessionID sessionId)
            throws RejectLogon {
        logonCheck(message, sessionId);
    }

    /**
     * This is a callback for application messages that we are being sending to a counterparty.
     * If we throw a DoNotSend exception in this function, the application will not send the message.
     * <p>
     * This is mostly useful if the application has been asked to resend a message such as an order that is no longer
     * relevant for the current market.
     * <p>
     * Messages that are being resent are marked with the PossDupFlag in the header set to true; If a DoNotSend
     * exception is thrown and the flag is set to true, a sequence reset will be sent in place of the message.
     * <p>
     * If it is set to false, the message will simply not be sent.
     * Notice that the FIX::Message is not const.
     * <p>
     * This allows us to add fields before an application message is sent out.
     *
     * @param message
     * @param sessionId
     */
    @Override
    public void toApp(final Message message, final SessionID sessionId) {

    }

    /**
     * This is one of the core entry points for our FIX application.
     * Every application level request will come through here.
     * <p>
     * If, for example, our application is a sell-side OMS, this is where we will get our new order requests.
     * If we were a buy side, we would get our execution reports here.
     * <p>
     * If a FieldNotFound exception is thrown, the counterparty will receive a reject indicating a conditionally
     * required field is missing. The Message class will throw this exception when trying to retrieve a missing field,
     * so we will rarely need to throw this explicitly.
     * <p>
     * We can also throw an UnsupportedMessageType exception.
     * This will result in the counterparty getting a reject informing them our application cannot process those types
     * of messages.
     * <p>
     * An IncorrectTagValue can also be thrown if a field contains a value that is out of range, or we do not support.
     *
     * @param message
     * @param sessionId
     * @throws FieldNotFound
     * @throws IncorrectTagValue
     * @throws UnsupportedMessageType
     */
    @Override
    public void fromApp(final Message message, final SessionID sessionId)
            throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionId);
    }

    private void logonCheck(final quickfix.Message message, final SessionID sessionID) throws RejectLogon {
        if (message instanceof Logon) {
            final Logon logon = (Logon) message;
            try {
                final Dictionary sessionSettings = settings.get(sessionID);
                final String userName = logon.getUsername().getValue();
                final String password = logon.getPassword().getValue();
                final String configuredUserName = sessionSettings.getString("UserName");
                final String configuredPassword = sessionSettings.getString("Password");
                if (!configuredUserName.equals(userName)) {
                    throw new RejectLogon(String.format("Username %s doesn't match the excepted one: %s",
                                                        userName,
                                                        configuredUserName));
                }
                if (!configuredPassword.equals(password)) {
                    throw new RejectLogon("Password mismatch.");
                }
            } catch (final Exception ex) {
                if (ex instanceof RejectLogon) {
                    throw new RejectLogon(ex.getMessage());
                }
                throw new RejectLogon(String.format("Unable to check logon credentials, due to: %s", ex.getMessage()));
            }
        }
    }

    public void onMessage(final quickfix.fix50.NewOrderSingle order, final SessionID sessionID)
            throws NumberFormatException, SQLException {
        executor.execute(new MatchingEngine(creditCheckConnectionPool, marketDataManager, order, sessionID));
    }

    public void cleanUp() {
        try {
            SimulatorUtils.shutdownExecutorService(executor, 5L, TimeUnit.SECONDS);
        } catch (final Exception ex) {
            logger.warn("Exception while shutting down the matching engine executor service.");
        }
        try {
            SimulatorUtils.shutdownExecutorService(scheduledExecutorService, 5L, TimeUnit.SECONDS);
        } catch (final Exception ex) {
            logger.warn("Exception while shutting down utility scheduled executor service.");
        }
        try {
            creditCheckConnectionPool.close();
        } catch (final SQLException ex) {
            logger.warn("Exception while closing database connection pool.");
        }
    }
}
