package com.raymondpang365.utility.order;

import quickfix.field.OrdType;

import java.util.Arrays;
import java.util.Optional;

public enum OrderType {

    MARKET("Market"), LIMIT("Limit");
    private final String typeName;

    private OrderType(final String name) {
        this.typeName = name;
    }

    @Override
    public String toString() {
        return typeName;
    }

    public OrdType toFIXOrderType() {
        switch (this) {
            case MARKET:
                return new OrdType(OrdType.MARKET);
            case LIMIT:
                return new OrdType(OrdType.LIMIT);
            default:
                throw new IllegalArgumentException(String.format("Unable to convert %s to FIX order type.", this));
        }
    }

    public static OrderType fromString(final String orderType) {
        final Optional<OrderType> result = Arrays.stream(OrderType.values()).filter(o -> o.typeName.equals(orderType)).findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        throw new IllegalArgumentException("Unknown order type: " + orderType);
    }
}
