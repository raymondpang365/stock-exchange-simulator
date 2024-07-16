package com.raymondpang365.server;

import com.raymondpang365.utility.DestinationType;
import com.raymondpang365.utility.ExchangeSimulatorLifeCycle;
import com.raymondpang365.utility.ExchangeSimulatorMessageConsumer;
import com.raymondpang365.utility.marketData.MarketData;
import com.raymondpang365.utility.utils.MarketDataUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Receives market data from a given queue.
 */
public class MarketDataManager implements MessageListener, ExchangeSimulatorLifeCycle {

    private final ExchangeSimulatorMessageConsumer marketDataConsumer;
    private final ConcurrentMap<String, MarketData> marketDataRepository;

    public MarketDataManager(final Properties properties) throws JMSException {
        marketDataRepository = new ConcurrentHashMap<>();
        marketDataConsumer
                = new ExchangeSimulatorMessageConsumer(properties.getProperty("activeMQ.url"),
                                                       properties.getProperty("activeMQ.marketDataTopic"),
                                                       DestinationType.Topic,
                                                       this,
                                                       "MarketDataManager",
                                                       null,
                                                       null);
    }

    @Override
    public void start() throws Exception {
        marketDataConsumer.start();
    }

    @Override
    public void stop() throws Exception {
        marketDataConsumer.stop();
    }

    @Override
    public void onMessage(final Message message) {
        try {
            @SuppressWarnings("unchecked") final ArrayList<MarketData> marketDataList
                    = (ArrayList<MarketData>) ((ObjectMessage) message).getObject();

            marketDataList.forEach(marketDataItem ->
                                           marketDataRepository.merge(marketDataItem.getSymbol(),
                                                                      marketDataItem,
                                                                      (oldValue, newValue) -> marketDataItem));
        } catch (final JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public MarketData get(final String symbol) {
        return marketDataRepository.getOrDefault(symbol, MarketDataUtils.buildRandomMarketDataTick(symbol));
    }
}
