package com.raymondpang365.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DestinationTypeTest {

    @Test
    void testDestinationTypeValues() {
        final DestinationType[] actual = DestinationType.values();
        assertArrayEquals(new DestinationType[]{DestinationType.Queue, DestinationType.Topic},
                          actual);
    }

    @Test
    void testDestinationTypeValueOf() {
        final DestinationType actual = DestinationType.valueOf("Queue");
        assertEquals(DestinationType.Queue, actual);
    }
}
