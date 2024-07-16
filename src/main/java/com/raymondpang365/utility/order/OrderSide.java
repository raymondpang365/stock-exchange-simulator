package com.raymondpang365.utility.order;

import quickfix.field.Side;

import java.util.Arrays;
import java.util.Optional;

public enum OrderSide {

    BUY("Buy"), SELL("Sell");

    private final String side;

    OrderSide(final String side) {
        this.side = side;
    }

    @Override
    public String toString() {
        return side;
    }

    public Side toFIXSide() {
        switch (this) {
            case BUY:
                return new Side(Side.BUY);
            case SELL:
                return new Side(Side.SELL);
            default:
                throw new IllegalArgumentException(String.format("Unable to convert %s to FIX side.", this));
        }
    }

    public static OrderSide fromString(final String orderSide) {
        final Optional<OrderSide> result = Arrays.stream(OrderSide.values())
                                                 .filter(o -> o.side.equals(orderSide))
                                                 .findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        throw new IllegalArgumentException(String.format("Unknown order side: %s", orderSide));
    }
}
