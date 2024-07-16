package com.raymondpang365.ui.model;

import com.raymondpang365.utility.order.Order;

import java.util.List;

/**
 * Custom table model with the initial data set coming from the orders stored in a MongoDB collection, then incremented
 * with the ones received onto the ExecutedOrdersTopic.
 */
public class FilledOrdersTableModel extends GenericListTableModel<Order> {

    private static final long serialVersionUID = 11L;

    public FilledOrdersTableModel(final List<Order> filledOrders) {
        super(filledOrders, new String[]{"ID",
                "Symbol",
                "Quantity",
                "Side",
                "Type",
                "Time in Force",
                "Fill Price",
                "Limit Price",
                "Fill Date",
                "Market Data ID"});
    }

    @Override
    public Object getValueAt(final int rowId, final int colId) {
        Object value = null;
        if (data.isEmpty()) {
            return null;
        }

        final Order order = data.get(rowId);
        switch (colId) {
            case 0:
                value = order.getId();
                break;
            case 1:
                value = order.getSymbol();
                break;
            case 2:
                value = order.getQuantity();
                break;
            case 3:
                value = order.getSide();
                break;
            case 4:
                value = order.getType();
                break;
            case 5:
                value = order.getTimeInForce();
                break;
            case 6:
                value = order.getAvgPx();
                break;
            case 7:
                value = order.getLimitPrice();
                break;
            case 8:
                value = order.getStoreDate();
                break;
            case 9:
                value = order.getMarketDataID();
                break;
            default:
                break;
        }

        return value;
    }
}
