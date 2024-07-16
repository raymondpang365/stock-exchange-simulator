package com.raymondpang365.orderRouter;

import com.raymondpang365.utility.DestinationType;
import com.raymondpang365.utility.ExchangeSimulatorMessageConsumer;
import com.raymondpang365.utility.ExchangeSimulatorMessageProducer;
import com.raymondpang365.utility.order.Order;
import com.raymondpang365.utility.order.OrderType;
import com.raymondpang365.utility.utils.SimulatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Session;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix50.NewOrderSingle;

import javax.jms.Message;
import javax.jms.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Listens to the OrdersQueue for orders and send to the FIX acceptor or Matching engine.
 * Publishes filled orders to the ExecutedOrdersTopic.
 */
public class FixInitiator implements Application, MessageListener, ExceptionListener {
    private static final Logger logger = LoggerFactory.getLogger(FixInitiator.class);

    private final SessionSettings settings;
    private final OrderManager orderManager;
    private final ExchangeSimulatorMessageConsumer ordersConsumer;
    private final ExchangeSimulatorMessageProducer executedOrdersProducer;
    private final Set<SessionID> loggedOnSessions;

    public FixInitiator(final SessionSettings settings) throws Exception {
        this.settings = settings;
        final Properties properties = SimulatorUtils.getApplicationProperties("stockExchangeSimulator.properties");
        orderManager = new OrderManager(new ConcurrentHashMap<>());
        loggedOnSessions = new HashSet<>();

        ordersConsumer
                = new ExchangeSimulatorMessageConsumer(properties.getProperty("activeMQ.url"),
                                                       properties.getProperty("activeMQ.ordersQueue"),
                                                       DestinationType.Queue,
                                                       this,
                                                       "FixInitiatorApplication",
                                                       null,
                                                       this);
        ordersConsumer.start();

        executedOrdersProducer
                = new ExchangeSimulatorMessageProducer(properties.getProperty("activeMQ.url"),
                                                       properties.getProperty("activeMQ.executedOrdersTopic"),
                                                       DestinationType.Topic,
                                                       "FixInitiatorApplication",
                                                       this); // should be null?
        executedOrdersProducer.start();
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
        logger.info("Session created: {}", sessionId);
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
        logger.info("Logon: {}", sessionId);
        loggedOnSessions.add(sessionId);
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
        logger.info("Logout: {}", sessionId);
        loggedOnSessions.remove(sessionId);
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
    public void toAdmin(final quickfix.Message message, final SessionID sessionId) {
        try {
            if (MsgType.LOGON.compareTo(message.getHeader().getString(MsgType.FIELD)) == 0) {
                final Dictionary dict = settings.get(sessionId);
                message.setString(quickfix.field.Username.FIELD, dict.getString("UserName"));
                message.setString(quickfix.field.Password.FIELD, dict.getString("Password"));
            }
        } catch (final Exception e) {
            logger.warn("Error setting user/password.", e);
        }
    }

    /**
     * This callback notifies us when an administrative message is sent from a counterparty to our FIX engine.
     * This can be useful for doing extra validation on logon messages such as for checking passwords.
     * Throwing a RejectLogon exception will disconnect the counterparty.
     *
     * @param message
     * @param sessionId
     */
    @Override
    public void fromAdmin(final quickfix.Message message, final SessionID sessionId) {

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
    public void toApp(final quickfix.Message message, final SessionID sessionId) {

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
     */
    @Override
    public void fromApp(final quickfix.Message message, final SessionID sessionId) {
        try {
            final MsgType msgType = new MsgType();
            if (message.getHeader().getField(msgType).valueEquals("8")) {
                executionReport(message, sessionId);
            }
        } catch (final Exception e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void onMessage(final Message message) {
        if (message instanceof ObjectMessage)
            try {
                loggedOnSessions.forEach(sessionId -> {
                    try {
                        final Order order = (Order) ((ObjectMessage) message).getObject();
                        order.setSessionID(sessionId);
                        send(order);
                    } catch (final Exception e) {
                        logger.error("Error deserializing order. {}", e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                });
            } catch (final Exception e) {
                logger.warn("Error receiving order. {}", e.getMessage());
            }
    }

    @Override
    public void onException(final JMSException jmsException) {
        logger.warn(jmsException.getMessage());
    }


    public void send(final Order order) {
        final NewOrderSingle newOrderSingle = new NewOrderSingle(new ClOrdID(order.getId()),
                                                                 order.getSide().toFIXSide(),
                                                                 new TransactTime(),
                                                                 order.getType().toFIXOrderType());
        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Symbol(order.getSymbol()));
        newOrderSingle.set(new HandlInst('1')); // DMA

        if (Objects.requireNonNull(order.getType()) == OrderType.LIMIT) {
            newOrderSingle.setField(new Price(order.getLimitPrice()));
        }
        // else market order

        newOrderSingle.setField(order.getTimeInForce().toFIXTimeInForce());

        try {
            Session.sendToTarget(newOrderSingle, order.getSessionID());
            orderManager.add(order);
            logger.info("Sent: {}", order);
        } catch (final SessionNotFound e) {
            logger.warn("Unable to send order", e);
        }
    }

    private void executionReport(final quickfix.Message message, final SessionID sessionID) throws FieldNotFound, JMSException {
        final Order order = orderManager.getOrder(message.getField(new ClOrdID()).getValue());
        if (order == null) {
            return;
        }

        try {
            order.setMessage(message.getField(new Text()).getValue());
        } catch (final FieldNotFound e) {
            logger.warn(e.getMessage());
        }

        final BigDecimal fillSize;
        final LeavesQty leavesQty = new LeavesQty();
        message.getField(leavesQty);

        fillSize = new BigDecimal(order.getQuantity())
                .subtract(BigDecimal.valueOf(leavesQty.getValue()));

        if (fillSize.compareTo(BigDecimal.ZERO) > 0) {
            // this is execution
            order.setOpen(order.getOpen() - (int) Double.parseDouble(fillSize.toPlainString()));
            order.setExecuted(new Integer(message.getString(CumQty.FIELD)));
            order.setAvgPx(new Double(message.getString(AvgPx.FIELD)));
        }

        final char ordStatus = message.getField(new OrdStatus()).getValue();
        switch (ordStatus) {
            case OrdStatus.REJECTED:
                order.setRejected(true);
                order.setOpen(0);
                if (message.isSetField(new Account())) {
                    order.setCreditCheckFailed(true);
                }

                final ObjectMessage m = executedOrdersProducer.getSession().createObjectMessage(order);
                m.setStringProperty("Status", "REJECTED");
                executedOrdersProducer.getProducer().send(m);
                break;

            case OrdStatus.CANCELED:
            case OrdStatus.DONE_FOR_DAY:
                order.setCanceled(true);
                order.setOpen(0);
                break;

            case OrdStatus.NEW:
                if (order.isNew()) {
                    order.setNew(false);
                }
                break;

            case OrdStatus.FILLED:
                order.setMarketDataID(message.getField(new Text()).getValue());
                final ObjectMessage m1 = executedOrdersProducer.getSession().createObjectMessage(order);
                m1.setStringProperty("Status", "FILLED");
                executedOrdersProducer.getProducer().send(m1);
                break;

            default:
                logger.warn("Unknown order status: {}", ordStatus);
        }

        orderManager.updateOrder(order);
    }

    public void closeOrdersConsumer() throws Exception {
        ordersConsumer.stop();
    }
}
