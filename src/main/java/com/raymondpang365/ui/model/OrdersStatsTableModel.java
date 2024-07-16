package com.raymondpang365.ui.model;

import java.util.List;

public class OrdersStatsTableModel extends GenericListTableModel<OrdersStats> {
    private static final long serialVersionUID = 19L;

    public OrdersStatsTableModel(final List<OrdersStats> orderStats) {
        super(orderStats, new String[]{"Total", "Filled", "Rejected", "Buy", "Sell", "Market", "Limit"});
    }

    @Override
    public Object getValueAt(final int rowId, final int colId) {
        Object value = null;
        if (data.isEmpty()) {
            return null;
        }

        final OrdersStats ordersStats = data.get(rowId);
        switch (colId) {
            case 0:
                value = ordersStats.getTotal();
                break;
            case 1:
                value = ordersStats.getFilled();
                break;
            case 2:
                value = ordersStats.getRejected();
                break;
            case 3:
                value = ordersStats.getBuy();
                break;
            case 4:
                value = ordersStats.getSell();
                break;
            case 5:
                value = ordersStats.getMarket();
                break;
            case 6:
                value = ordersStats.getLimit();
                break;
            default:
                break;
        }

        return value;
    }
}
