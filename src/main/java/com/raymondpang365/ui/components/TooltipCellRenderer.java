package com.raymondpang365.ui.components;

import com.raymondpang365.utility.marketData.MarketData;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public final class TooltipCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;

    private final List<MarketData> marketDataItems;

    public TooltipCellRenderer(final List<MarketData> marketDataItems) {
        this.marketDataItems = marketDataItems;
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table,
                                                   final Object value,
                                                   final boolean isSelected,
                                                   final boolean hasFocus,
                                                   final int row,
                                                   final int column) {
        final JLabel label = (JLabel) super.getTableCellRendererComponent(table,
                                                                          value,
                                                                          isSelected,
                                                                          hasFocus,
                                                                          row,
                                                                          column);
        final Optional<MarketData> marketDataItem =
                marketDataItems.stream()
                               .filter((m) -> m.getId()
                                               .equals(value))
                               .findFirst();
        if (marketDataItem.isPresent()) {
            label.setToolTipText(marketDataItem.toString());
        }
        return label;
    }
}
