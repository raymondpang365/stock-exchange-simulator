package com.raymondpang365.utility.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    @Test
    @DisplayName("Test Order default constructor")
    void testOrderDefaultConstructor() {
        final Order order = new Order();
        assertNotNull(order);
        System.out.println(order);

        assertNotNull(order.getId());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(OrderType.MARKET, order.getType());
        assertEquals(OrderTimeInForce.DAY, order.getTimeInForce());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ID1",
            "ID2"
    })
    @DisplayName("Test Order single parameter constructor")
    void testOrderSingleParameterConstructor(final String id) {
        final Order order = new Order(id);
        assertNotNull(order);
        System.out.println(order);

        assertEquals(id, order.getId());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(OrderType.MARKET, order.getType());
        assertEquals(OrderTimeInForce.DAY, order.getTimeInForce());
    }

    @Test
    @DisplayName("Test Order multiple parameters constructor")
    void testOrderMultipleParametersConstructor() {
        final Order order
                = new Order("id", "symbol", 10, OrderSide.SELL, OrderType.LIMIT, OrderTimeInForce.IOC,
                            50D, 40D, "originalID", LocalDateTime.now(ZoneId.of("UTC")),
                            false, "marketDataID", false);
        assertNotNull(order);
        System.out.println(order);

        assertEquals("id", order.getId());
        assertEquals("symbol", order.getSymbol());
        assertEquals(10, order.getQuantity());
        assertEquals(OrderSide.SELL, order.getSide());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals(OrderTimeInForce.IOC, order.getTimeInForce());
        assertEquals(50D, order.getLimitPrice());
        assertEquals(40D, order.getAvgPx());
        assertEquals("originalID", order.getOriginalID());
        assertNotNull(order.getStoreDate());
        assertFalse(order.isRejected());
        assertEquals("marketDataID", order.getMarketDataID());
        assertFalse(order.isCreditCheckFailed());
    }
}
