package com.raymondpang365.services.database;

import com.raymondpang365.utility.marketData.MarketData;
import com.raymondpang365.utility.order.Order;
import com.raymondpang365.utility.order.OrderType;

import java.util.List;
import java.util.Optional;

public interface DataManager extends AutoCloseable {

    /**
     * Stores an order in the back-end.
     *
     * @param order Order to store.
     */
    void storeOrder(Order order);

    /**
     * Gets the orders from the back-end.
     *
     * @param orderType If passed, then it only retrieves orders with the given type.
     * @return List of orders.
     */
    List<Order> getOrders(Optional<OrderType> orderType);

    /**
     * Stores a list of market data items in the back-end.
     *
     * @param marketDataItems List of market data items.
     * @param deleteFirst     If true, then it deletes all market data items before adding the ones passed in.
     */
    void storeMarketDataItems(List<MarketData> marketDataItems, boolean deleteFirst);

    /**
     * Gets the market data from the back-end.
     *
     * @param symbol If passed, then it only retrieves market data with the given symbol.
     * @return List of market data items.
     */
    List<MarketData> getMarketData(Optional<String> symbol);

}
