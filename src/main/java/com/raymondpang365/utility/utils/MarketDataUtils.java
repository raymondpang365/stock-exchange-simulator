package com.raymondpang365.utility.utils;

import com.raymondpang365.utility.marketData.MarketData;

import java.util.concurrent.ThreadLocalRandom;

import static com.raymondpang365.utility.utils.NumberUtils.roundDouble;

public final class MarketDataUtils {

    private MarketDataUtils() {
    }

    public static MarketData buildRandomMarketDataTick(final String symbol) {
        return new MarketData(symbol,
                              roundDouble(ThreadLocalRandom.current().nextDouble() * 100, 2),
                              roundDouble(ThreadLocalRandom.current().nextDouble() * 100, 2),
                              ThreadLocalRandom.current().nextInt(1000),
                              ThreadLocalRandom.current().nextInt(1000)
        );
    }
}
