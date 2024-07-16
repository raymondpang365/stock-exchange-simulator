package com.raymondpang365.utility.marketData;

import com.raymondpang365.utility.utils.MarketDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MarketDataTest {

    @ParameterizedTest
    @CsvSource({
            "ID1, ABC",
            "ID2, XYZ"
    })
    @DisplayName("Test MarketData constructor")
    void testMarketDataConstructor(final String id, final String symbol) {
        final MarketData marketData
                = new MarketData(id, symbol, 1D, 1D, 10, 10,
                                 LocalDateTime.now(ZoneId.of("UTC")));
        assertNotNull(marketData);
        System.out.println(marketData);

        assertEquals(id, marketData.getId());
        assertEquals(symbol, marketData.getSymbol());
        assertEquals(1D, marketData.getBid());
        assertEquals(1D, marketData.getAsk());
        assertEquals(10D, marketData.getBidSize());
        assertEquals(10D, marketData.getAskSize());
        assertNotNull(marketData.getQuoteDateTime());
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "ABC",
            "XYZ"
    })
    @DisplayName("Test building random Market Data Tick")
    void testBuildingRandomMarketDataTick(final String symbol) {
        final MarketData marketDataTick = MarketDataUtils.buildRandomMarketDataTick(symbol);
        assertNotNull(marketDataTick);
        System.out.println(marketDataTick);

        assertEquals(symbol, marketDataTick.getSymbol());
    }
}
