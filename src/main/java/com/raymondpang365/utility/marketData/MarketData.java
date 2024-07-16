package com.raymondpang365.utility.marketData;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public final class MarketData implements Serializable {

    private static final long serialVersionUID = 47L;

    private final String symbol;
    private final double bid;
    private final double ask;
    private final int bidSize;
    private final int askSize;

    private LocalDateTime quoteDateTime;

    private String id;

    /**
     * This is used to build a new instance.
     *
     * @param symbol
     * @param bid
     * @param ask
     * @param bidSize
     * @param askSize
     */
    public MarketData(final String symbol, final double bid, final double ask, final int bidSize, final int askSize) {
        this.symbol = symbol;
        this.bid = bid;
        this.ask = ask;
        this.bidSize = bidSize;
        this.askSize = askSize;
        this.quoteDateTime = LocalDateTime.now(ZoneId.of("UTC"));
        this.id = UUID.randomUUID().toString();
    }

    /**
     * This is used to set a new instance when loaded from a back-end store.
     *
     * @param id
     * @param symbol
     * @param bid
     * @param ask
     * @param bidSize
     * @param askSize
     * @param quoteDateTime
     */
    public MarketData(final String id,
                      final String symbol,
                      final double bid,
                      final double ask,
                      final int bidSize,
                      final int askSize,
                      final LocalDateTime quoteDateTime) {
        this(symbol, bid, ask, bidSize, askSize);
        this.quoteDateTime = quoteDateTime;
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    public int getBidSize() {
        return bidSize;
    }

    public int getAskSize() {
        return askSize;
    }

    public LocalDateTime getQuoteDateTime() {
        return quoteDateTime;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "MarketData{" +
                "symbol='" + symbol + '\'' +
                ", bid=" + bid +
                ", ask=" + ask +
                ", bidSize=" + bidSize +
                ", askSize=" + askSize +
                ", quoteDateTime=" + quoteDateTime +
                ", id='" + id + '\'' +
                '}';
    }

}
