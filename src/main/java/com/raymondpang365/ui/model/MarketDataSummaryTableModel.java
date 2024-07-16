package com.raymondpang365.ui.model;

import java.util.List;

public class MarketDataSummaryTableModel extends GenericListTableModel<MarketDataSummary> {

    private static final long serialVersionUID = 13L;

    public MarketDataSummaryTableModel(final List<MarketDataSummary> marketDataSummaryItems) {
        super(marketDataSummaryItems, new String[]{"Symbol",
                "Avg. Bid",
                "Avg. Ask",
                "Avg. BidSize",
                "Avg. AskSize",
                "No. of Items"});
    }

    @Override
    public Object getValueAt(final int rowId, final int colId) {
        Object value = null;
        if (data.isEmpty()) {
            return null;
        }

        final MarketDataSummary marketDataSummary = data.get(rowId);
        switch (colId) {
            case 0:
                value = marketDataSummary.getSymbol();
                break;
            case 1:
                value = marketDataSummary.getAvgBid();
                break;
            case 2:
                value = marketDataSummary.getAvgAsk();
                break;
            case 3:
                value = marketDataSummary.getAvgBidSize();
                break;
            case 4:
                value = marketDataSummary.getAvgAskSize();
                break;
            case 5:
                value = marketDataSummary.getItemsNumber();
                break;
            default:
                break;
        }

        return value;
    }
}
