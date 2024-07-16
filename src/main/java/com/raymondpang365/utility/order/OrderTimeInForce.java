package com.raymondpang365.utility.order;

import quickfix.field.TimeInForce;

import java.util.Arrays;
import java.util.Optional;

public enum OrderTimeInForce {

    DAY("Day"), IOC("IOC"), FOK("FOK");

    private final String timeInForce;

    private OrderTimeInForce(final String timeInForce) {
        this.timeInForce = timeInForce;
    }

    public String toString() {
        return timeInForce;
    }

    public TimeInForce toFIXTimeInForce() {
        switch (this) {
            case DAY:
                return new TimeInForce(TimeInForce.DAY);
            case IOC:
                return new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL);
            case FOK:
                return new TimeInForce(TimeInForce.FILL_OR_KILL);
            default:
                throw new IllegalArgumentException(String.format("Unable to convert %s to FIX time in force.", this));
        }
    }

    public static OrderTimeInForce fromString(final String orderTimeInForce) {
        final Optional<OrderTimeInForce> result = Arrays.stream(OrderTimeInForce.values()).filter(o -> o.timeInForce.equals(orderTimeInForce)).findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        throw new IllegalArgumentException(String.format("Unknown order time in force: %s", orderTimeInForce));
    }

}
