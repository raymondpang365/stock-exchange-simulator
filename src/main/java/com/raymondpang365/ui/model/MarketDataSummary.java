package com.raymondpang365.ui.model;

public final class MarketDataSummary {
    private final String symbol;
    private final double avgBid;
    private final double avgAsk;
    private final double avgBidSize;
    private final double avgAskSize;
    private final long itemsNumber;

    public MarketDataSummary(final String symbol,
                             final double avgBid,
                             final double avgAsk,
                             final double avgBidSize,
                             final double avgAskSize,
                             final long itemsNumber) {
        this.symbol = symbol;
        this.avgBid = avgBid;
        this.avgAsk = avgAsk;
        this.avgBidSize = avgBidSize;
        this.avgAskSize = avgAskSize;
        this.itemsNumber = itemsNumber;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getAvgBid() {
        return avgBid;
    }

    public double getAvgAsk() {
        return avgAsk;
    }

    public double getAvgBidSize() {
        return avgBidSize;
    }

    public double getAvgAskSize() {
        return avgAskSize;
    }

    public long getItemsNumber() {
        return itemsNumber;
    }

    @Override
    public String toString() {
        return "MarketDataSummary{" +
                "symbol='" + symbol + '\'' +
                ", avgBid=" + avgBid +
                ", avgAsk=" + avgAsk +
                ", avgBidSize=" + avgBidSize +
                ", avgAskSize=" + avgAskSize +
                ", itemsNumber=" + itemsNumber +
                '}';
    }
}
