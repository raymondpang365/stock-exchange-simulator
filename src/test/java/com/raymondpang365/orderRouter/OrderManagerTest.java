package com.raymondpang365.orderRouter;

import com.raymondpang365.utility.order.Order;
import com.raymondpang365.utility.utils.RandomOrdersBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderManagerTest {

    @Mock
    private ConcurrentMap<String, Order> orders;

    @InjectMocks
    private OrderManager orderManager;

    Order order;

    @BeforeEach
    void setUp() {
        order = RandomOrdersBuilder.build(Collections.singletonList("AAPL"));
    }

    @Test
    void testAdd() {
        // when
        orderManager.add(order);

        // then
        verify(orders, times(1)).putIfAbsent(order.getId(), order);
    }

    @Test
    void testGetOrder() {
        // given
        final String id = order.getId();
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        // when
        orderManager.getOrder(id);

        // then
        verify(orders, times(1)).get(captor.capture());
        assertEquals(id, captor.getValue());
    }

    @Test
    void testUpdateOrder() {
        // when
        orderManager.updateOrder(order);

        // then
        verify(orders, times(1)).put(order.getId(), order);
    }
}
