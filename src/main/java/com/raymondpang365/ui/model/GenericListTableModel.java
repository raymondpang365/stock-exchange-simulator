package com.raymondpang365.ui.model;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Custom table model with the initial data set coming from the database.
 */
public abstract class GenericListTableModel<T> extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private final String[] columnNames;
    protected final List<T> data;

    public GenericListTableModel(final List<T> data, final String[] columnNames) {
        super();
        this.columnNames = columnNames;
        this.data = data;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public String getColumnName(final int colId) {
        return columnNames[colId];
    }

    @Override
    public Class<?> getColumnClass(final int c) {
        final Object valueAt = getValueAt(0, c);
        if (valueAt == null)
            return Object.class;
        return valueAt.getClass();
    }
}
