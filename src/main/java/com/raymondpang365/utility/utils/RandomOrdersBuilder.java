package com.raymondpang365.utility.utils;

import com.raymondpang365.utility.order.Order;
import com.raymondpang365.utility.order.OrderSide;
import com.raymondpang365.utility.order.OrderTimeInForce;
import com.raymondpang365.utility.order.OrderType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomOrdersBuilder {

    private static final Random randomGenerator = ThreadLocalRandom.current();

    public static Order build(final List<String> allowedSymbols) {
        final Order order = new Order(); // id set
        order.setSymbol(randomListValue(allowedSymbols));
        order.setQuantity(randomGenerator.nextInt(1000) + 1);
        order.setSide(randomEnumValue(OrderSide.class));
        final OrderType randomOrderType = randomEnumValue(OrderType.class);
        order.setType(randomOrderType);
        order.setTimeInForce(randomEnumValue(OrderTimeInForce.class));
        if (Objects.requireNonNull(randomOrderType) == OrderType.LIMIT) {
            order.setLimitPrice(NumberUtils.roundDouble(randomGenerator.nextDouble() * 100, 2));
        }
        order.setStoreDate(LocalDateTime.now(ZoneId.of("UTC")));
        order.setRejected(randomGenerator.nextBoolean());
        return order;
    }

    private static <T extends Enum<?>> T randomEnumValue(final Class<T> enumClass) {
        return enumClass.getEnumConstants()[randomGenerator.nextInt(enumClass.getEnumConstants().length)];
    }

    private static <T> T randomListValue(final List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(randomGenerator.nextInt(list.size()));
    }
}
