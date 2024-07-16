package com.raymondpang365.ui;

import com.raymondpang365.services.database.noSql.MongoDBConnection;
import com.raymondpang365.services.database.noSql.MongoDBManager;
import com.raymondpang365.ui.components.MarketDataPanel;
import com.raymondpang365.ui.components.OrdersPanel;
import com.raymondpang365.utility.DestinationType;
import com.raymondpang365.utility.ExchangeSimulatorMessageConsumer;
import com.raymondpang365.utility.database.DatabaseProperties;
import com.raymondpang365.utility.marketData.MarketData;
import com.raymondpang365.utility.order.Order;
import com.raymondpang365.utility.utils.MarketDataUtils;
import com.raymondpang365.utility.utils.RandomOrdersBuilder;
import com.raymondpang365.utility.utils.SimulatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.raymondpang365.utility.utils.SimulatorUtils.lineSeparator;

/**
 * Creates and shows the application, which displays filled and rejected orders.
 * Initially, the respective JTables are filled with orders coming from MongoDB repository. Then, they get live updates
 * from the executedOrdersTopic.
 * Upon application shutdown, it closes MongoDB and topic subscriber connections.
 */
public class ExchangeSimulatorUI implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeSimulatorUI.class);
    private static final boolean isWithoutLiveFeed = false;

    private final ExchangeSimulatorMessageConsumer executedOrdersConsumer;
    private final ExchangeSimulatorMessageConsumer marketDataConsumer;
    private final MongoDBManager mongoDBManager;
    private final List<Order> filledOrders;
    private final List<Order> rejectedOrders;
    private final List<MarketData> marketDataItems;
    private final OrdersPanel ordersPanel;
    private final MarketDataPanel marketDataPanel;


    public ExchangeSimulatorUI(final Properties p) throws Exception {
        mongoDBManager = new MongoDBManager(new MongoDBConnection(
                new DatabaseProperties(p.getProperty("mongoDB.host"),
                                       Integer.parseInt(p.getProperty("mongoDB.port")),
                                       p.getProperty("mongoDB.database"))),
                                            p.getProperty("mongoDB.executedOrdersCollection"),
                                            p.getProperty("mongoDB.marketDataCollection"));

        executedOrdersConsumer = new ExchangeSimulatorMessageConsumer(p.getProperty("activeMQ.url"),
                                                                      p.getProperty("activeMQ.executedOrdersTopic"),
                                                                      DestinationType.Topic,
                                                                      this,
                                                                      "ExchangeSimulatorExecutedOrdersConsumer",
                                                                      null,
                                                                      null);

        marketDataConsumer = new ExchangeSimulatorMessageConsumer(p.getProperty("activeMQ.url"),
                                                                  p.getProperty("activeMQ.marketDataTopic"),
                                                                  DestinationType.Topic,
                                                                  this,
                                                                  "ExchangeSimulatorMarketDataConsumer",
                                                                  null,
                                                                  null);

        marketDataItems = mongoDBManager.getMarketData(Optional.empty());
        final List<Order> backEndOrders = mongoDBManager.getOrders(Optional.empty());

        final Comparator<? super Order> dateComparator = Comparator.comparing(Order::getStoreDate);
        filledOrders = backEndOrders.stream()
                                    .filter(o -> !o.isRejected())
                                    .sorted(dateComparator.reversed())
                                    .collect(Collectors.toList());

        rejectedOrders = backEndOrders.stream()
                                      .filter(Order::isRejected)
                                      .sorted(dateComparator.reversed())
                                      .collect(Collectors.toList());

        ordersPanel = new OrdersPanel(filledOrders, rejectedOrders);
        //ordersPanel.getFilledOrdersTable()
        // .getColumnModel()
        // .getColumn(10)
        // .setCellRenderer(new TooltipCellRenderer(marketDataItems));

        marketDataPanel = new MarketDataPanel(marketDataItems);
        executedOrdersConsumer.start();
        marketDataConsumer.start();

        if (isWithoutLiveFeed) {
            simulationWithoutLiveFeed();
        }
    }

    public void show() {
        final JFrame frame = new JFrame("Trade Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTabbedPane tb = new JTabbedPane();
        tb.addTab("Orders", ordersPanel);
        tb.addTab("Market Data", marketDataPanel);
        tb.setSelectedIndex(0);
        tb.setOpaque(true);
        frame.setContentPane(tb);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                try {
                    mongoDBManager.close();
                } catch (final Exception e1) {
                    logger.warn(String.format("Unable to close the MongoDB connection.%s%s", e1.getMessage(),
                                              lineSeparator));
                }
                try {
                    executedOrdersConsumer.stop();
                } catch (final Exception e1) {
                    logger.warn(String.format("Unable to close the executedOrdersConsumer topic subscriber.%s%s", e1.getMessage(),
                                              lineSeparator));
                }
                try {
                    marketDataConsumer.stop();
                } catch (final Exception e1) {
                    logger.warn(String.format("Unable to close the marketDataConsumer topic subscriber.%s%s", e1.getMessage(),
                                              lineSeparator));
                }
                try {
                    ordersPanel.cleanUp();
                    marketDataPanel.cleanUp();
                } catch (final Exception e1) {
                    logger.warn(String.format("Unable to clean up OrdersPanel resources.%s%s", e1.getMessage(),
                                              lineSeparator));
                }
            }
        });

        frame.setLocation(new Point(300, 300));
        frame.setSize(new Dimension(1500, 700));
        frame.setVisible(true);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(final Message message) {
        try {
            final Serializable objectMessage = ((ObjectMessage) message).getObject();
            if (objectMessage instanceof Order) {
                //gets the filled order and updates and notifies the table model accordingly.
                final Order order = (Order) objectMessage;
                if (order.isRejected()) {
                    rejectedOrders.add(order);
                    ((AbstractTableModel) ordersPanel.getRejectedOrdersTable().getModel()).fireTableDataChanged();
                } else {
                    filledOrders.add(order);
                    ((AbstractTableModel) ordersPanel.getFilledOrdersTable().getModel()).fireTableDataChanged();
                }
            } else if (objectMessage instanceof ArrayList<?>) {
                marketDataItems.addAll((ArrayList<MarketData>) objectMessage);
                ((AbstractTableModel) marketDataPanel.getMarketDataTable().getModel()).fireTableDataChanged();
            }
        } catch (final JMSException e) {
            logger.warn("Failed to process object message, due to " + e.getMessage());
        }
    }

    private void simulationWithoutLiveFeed() {
        Executors.newSingleThreadScheduledExecutor()
                 .execute(() ->
                                  IntStream
                                          .range(1, 100000)
                                          .forEach(i ->
                                                   {
                                                       Stream.of("AAPL", "META", "TSLA", "GOOGL", "IBM", "ORCL", "NFLX", "MSFT", "AMZN")
                                                             .forEach(a -> {
                                                                 marketDataItems.add(MarketDataUtils.buildRandomMarketDataTick(a));
                                                                 ((AbstractTableModel) marketDataPanel.getMarketDataTable()
                                                                                                      .getModel())
                                                                         .fireTableDataChanged();
                                                             });
                                                       try {
                                                           TimeUnit.MILLISECONDS.sleep(1000L);
                                                       } catch (final Exception e) {
                                                           logger.warn(e.getMessage());
                                                       }
                                                   }));

        Executors.newSingleThreadScheduledExecutor().execute(
                () -> IntStream
                        .range(0, 10000)
                        .forEach(i -> {
                            final Order randomOrder = RandomOrdersBuilder.build(Arrays.asList("AAPL", "META", "TSLA"));
                            if (randomOrder.isRejected()) {
                                rejectedOrders.add(randomOrder);
                                ((AbstractTableModel) ordersPanel.getRejectedOrdersTable().getModel())
                                        .fireTableDataChanged();
                            } else {
                                filledOrders.add(randomOrder);
                                ((AbstractTableModel) ordersPanel.getFilledOrdersTable().getModel())
                                        .fireTableDataChanged();
                            }
                            try {
                                TimeUnit.MILLISECONDS.sleep(500L);
                            } catch (final Exception e) {
                                logger.warn(e.getMessage());
                            }
                        }));
    }

    public static void main(final String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                new ExchangeSimulatorUI(SimulatorUtils
                                                .getApplicationProperties("stockExchangeSimulator.properties"))
                        .show();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


}
