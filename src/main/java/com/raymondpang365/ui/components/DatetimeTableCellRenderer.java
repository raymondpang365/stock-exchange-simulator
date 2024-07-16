package com.raymondpang365.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DatetimeTableCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 71L;

    @Override
    public Component getTableCellRendererComponent(final JTable table,
                                                   final Object value,
                                                   final boolean isSelected,
                                                   final boolean hasFocus,
                                                   final int row,
                                                   final int column) {
        LocalDateTime ldtValue = LocalDateTime.now();
        if (value instanceof LocalDateTime) {
            ldtValue = (LocalDateTime) value;
        }
        return super.getTableCellRendererComponent(table,
                                                   ldtValue.format(DateTimeFormatter.ISO_DATE_TIME),
                                                   isSelected,
                                                   hasFocus,
                                                   row,
                                                   column);
    }
}
