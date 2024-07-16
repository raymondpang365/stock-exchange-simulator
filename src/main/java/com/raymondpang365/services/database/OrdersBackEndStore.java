package com.raymondpang365.services.database;

import com.raymondpang365.services.database.noSql.MongoDBConnection;
import com.raymondpang365.services.database.noSql.MongoDBManager;
import com.raymondpang365.services.database.sql.MySqlManager;
import com.raymondpang365.utility.DestinationType;
import com.raymondpang365.utility.ExchangeSimulatorLifeCycle;
import com.raymondpang365.utility.ExchangeSimulatorMessageConsumer;
import com.raymondpang365.utility.database.DatabaseProperties;
import com.raymondpang365.utility.database.MySqlConnection;
import com.raymondpang365.utility.database.creditCheck.CreditCheck;
import com.raymondpang365.utility.database.creditCheck.ICreditCheck;
import com.raymondpang365.utility.order.Order;
import com.raymondpang365.utility.utils.NumberUtils;
import com.raymondpang365.utility.utils.SimulatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class OrdersBackEndStore implements MessageListener, ExchangeSimulatorLifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(OrdersBackEndStore.class);
    private static final Random randomGenerator = ThreadLocalRandom.current();

    private final ExchangeSimulatorMessageConsumer ordersConsumer;
    private final DataManager mongoDBManager;
    private final DataManager mySqlManager;
    private final ICreditCheck creditCheck;

    public OrdersBackEndStore(final Properties props) throws JMSException, SQLException {
        ordersConsumer = new ExchangeSimulatorMessageConsumer(props.getProperty("activeMQ.url"),
                                                              props.getProperty("activeMQ.executedOrdersTopic"),
                                                              DestinationType.Topic,
                                                              this,
                                                              "BackEnd",
                                                              null,
                                                              null);

        mongoDBManager = new MongoDBManager(
                new MongoDBConnection(
                        new DatabaseProperties(props.getProperty("mongoDB.host"),
                                               Integer.parseInt(props.getProperty("mongoDB.port")),
                                               props.getProperty("mongoDB.database"))),
                props.getProperty("mongoDB.executedOrdersCollection"));

        final MySqlConnection mySqlConnection = new MySqlConnection(
                new DatabaseProperties(props.getProperty("mySQL.host"),
                                       Integer.parseInt(props.getProperty("mySQL.port")),
                                       props.getProperty("mySQL.database"),
                                       props.getProperty("mySQL.userName"),
                                       props.getProperty("mySQL.password")));

        mySqlManager = new MySqlManager(mySqlConnection);
        creditCheck = new CreditCheck(mySqlConnection.getConnection());
    }

    @Override
    public void start() throws Exception {
        ordersConsumer.start();
    }

    @Override
    public void stop() throws Exception {
        ordersConsumer.stop();
        mongoDBManager.close();
        mySqlManager.close();
    }

    @Override
    public void onMessage(final Message message) {
        try {
            final Order order = (Order) ((ObjectMessage) message).getObject();
            if (order.isCreditCheckFailed()) { // reset the credit
                creditCheck.setCredit(NumberUtils.roundDouble(randomGenerator.nextDouble() * 99999, 2));
            }
            mongoDBManager.storeOrder(order);
            mySqlManager.storeOrder(order);
        } catch (final JMSException e) {
            logger.warn("Failed to persist order, due to {}", e.getMessage());
        }
    }

    public static void main(final String[] args) throws Exception {
        final OrdersBackEndStore f = new OrdersBackEndStore(
                SimulatorUtils.getApplicationProperties("stockExchangeSimulator.properties"));
        f.start();
        TimeUnit.SECONDS.sleep(10L);
        f.stop();
    }
}
