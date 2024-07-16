package com.raymondpang365.orderRouter;

import com.raymondpang365.utility.order.Order;

import java.util.concurrent.ConcurrentMap;

/**
 * Trace all orders sent to the acceptor and update them when come back.
 */
public class OrderManager {

    private final ConcurrentMap<String, Order> orders;

    public OrderManager(final ConcurrentMap<String, Order> orders) {
        this.orders = orders;
    }

    public void add(final Order order) {
        orders.putIfAbsent(order.getId(), order);
    }

    public Order getOrder(final String orderId) {
        return orders.get(orderId);
    }

    public void updateOrder(final Order order) {
        orders.put(order.getId(), order);
    }
}
