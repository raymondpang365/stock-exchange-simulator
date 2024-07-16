package com.raymondpang365.ui.model;

import com.raymondpang365.utility.marketData.MarketData;

import java.util.List;

/**
 * Custom table model with the initial data set coming from the market data stored in a MongoDB collection, then
 * incremented with updates coming from the topic MarketDataTopic.
 */
public class MarketDataTableModel extends GenericListTableModel<MarketData> {

    private static final long serialVersionUID = 17L;

    public MarketDataTableModel(final List<MarketData> marketDataItems) {
        super(marketDataItems, new String[]{"ID", "Symbol", "Bid", "Ask", "BidSize", "AskSize", "QuoteTime"});
    }

    @Override
    public Object getValueAt(final int rowId, final int colId) {
        Object value = null;
        if (data.isEmpty()) {
            return null;
        }

        final MarketData marketData = data.get(rowId);
        switch (colId) {
            case 0:
                value = marketData.getId();
                break;
            case 1:
                value = marketData.getSymbol();
                break;
            case 2:
                value = marketData.getBid();
                break;
            case 3:
                value = marketData.getAsk();
                break;
            case 4:
                value = marketData.getBidSize();
                break;
            case 5:
                value = marketData.getAskSize();
                break;
            case 6:
                value = marketData.getQuoteDateTime();
                break;
            default:
                break;
        }

        return value;
    }

}
