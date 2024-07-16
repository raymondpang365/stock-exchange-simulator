package com.raymondpang365.utility.utils;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.List;

public final class SwingUtils {

    private SwingUtils() {
    }

    public static void setTableSorter(final JTable table, final int sortColumn, final SortOrder sortOrder) {
        final TableRowSorter<TableModel> tableSorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(tableSorter);
        final List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(sortColumn, sortOrder));
        tableSorter.setSortKeys(sortKeys);
        tableSorter.sort();
    }

}
