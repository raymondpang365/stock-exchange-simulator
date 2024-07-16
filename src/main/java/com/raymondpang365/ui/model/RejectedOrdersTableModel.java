package com.raymondpang365.ui.model;

import com.raymondpang365.utility.order.Order;

import java.util.List;

public final class RejectedOrdersTableModel extends GenericListTableModel<Order> {

    private static final long serialVersionUID = 43L;

    public RejectedOrdersTableModel(final List<Order> rejectedOrders) {
        super(rejectedOrders, new String[]{"ID",
                "Symbol",
                "Quantity",
                "Side",
                "Type",
                "Time in Force",
                "Limit Price",
                "Reject Date",
                "Credit Check Failed"});
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
                value = order.getLimitPrice();
                break;
            case 7:
                value = order.getStoreDate();
                break;
            case 8:
                value = order.isCreditCheckFailed();
                break;
        }

        return value;
    }
}
