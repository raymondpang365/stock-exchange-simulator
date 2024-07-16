package com.raymondpang365.ui.components;

import com.raymondpang365.ui.model.FilledOrdersTableModel;
import com.raymondpang365.ui.model.OrdersStats;
import com.raymondpang365.ui.model.OrdersStatsTableModel;
import com.raymondpang365.ui.model.RejectedOrdersTableModel;
import com.raymondpang365.utility.order.Order;
import com.raymondpang365.utility.order.OrderSide;
import com.raymondpang365.utility.order.OrderType;
import com.raymondpang365.utility.utils.SimulatorUtils;
import com.raymondpang365.utility.utils.SwingUtils;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * It creates a panel with an executed and rejected orders tables and:
 * <ul>
 * 	<li>Sets a column renderer on the FilledDate column.</li>
 * 	<li>Overrides JTable.prepareRenderer in order to show BUY and SELL order rows with different colors.</li>
 * 	<li>Sets a TableRowSorter with default sorting enabled on the FilledDate column.</li>
 * 	<li>As a side content, North to the tables, it shows statistics about the orders, which get updated every 1 second.</li>
 * </ul>
 */
public final class OrdersPanel extends JPanel implements PanelCleanUp {
    private static final long serialVersionUID = 1L;

    private final List<OrdersStats> ordersStats;
    private final JTable filledOrdersTable;
    private final JTable rejectedOrdersTable;
    private final JTable ordersStatsTable;
    private final ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();

    private static OrdersStats buildOrdersStats(final List<Order> allOrders) {
        final Map<Boolean, Long> groupedByRejectionStatus = allOrders
                .parallelStream()
                .collect(Collectors.partitioningBy(Order::isRejected,
                                                   Collectors.counting()));

        final Map<OrderSide, Long> groupedBySide = allOrders
                .parallelStream()
                .collect(Collectors.groupingBy(Order::getSide,
                                               Collectors.counting()));

        final Map<OrderType, Long> groupedByType = allOrders
                .parallelStream()
                .collect(Collectors.groupingBy(Order::getType,
                                               Collectors.counting()));

        return new OrdersStats(allOrders.size(),
                               groupedByRejectionStatus.getOrDefault(false, 0L),
                               groupedByRejectionStatus.getOrDefault(true, 0L),
                               groupedBySide.getOrDefault(OrderSide.BUY, 0L),
                               groupedBySide.getOrDefault(OrderSide.SELL, 0L),
                               groupedByType.getOrDefault(OrderType.MARKET, 0L),
                               groupedByType.getOrDefault(OrderType.LIMIT, 0L));
    }

    public OrdersPanel(final List<Order> filledOrders, final List<Order> rejectedOrders) {
        super(new BorderLayout(10, 20));
        filledOrdersTable = buildOrdersTable(filledOrders, true);
        rejectedOrdersTable = buildOrdersTable(rejectedOrders, false);

        ordersStats = Collections.singletonList(buildOrdersStats(
                Stream.concat(filledOrders.stream(),
                              rejectedOrders.stream())
                      .collect(Collectors.toList())));

        ordersStatsTable = new JTable(new OrdersStatsTableModel(ordersStats));
        ordersStatsTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        ordersStatsTable.setFillsViewportHeight(true);
        add(ordersStatsTable, BorderLayout.NORTH);

        final JScrollPane filledOrdersScrollPane = new JScrollPane(filledOrdersTable);
        filledOrdersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Filled Orders"));
        add(filledOrdersScrollPane, BorderLayout.CENTER);

        final JScrollPane rejectedOrdersScrollPane = new JScrollPane(rejectedOrdersTable);
        rejectedOrdersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Rejected Orders"));
        add(rejectedOrdersScrollPane, BorderLayout.SOUTH);

        // Orders stats get updated every 1 sec.
        es.scheduleWithFixedDelay(() -> {
            ordersStats.clear();
            ordersStats.add(buildOrdersStats(
                    java.util.stream.Stream.concat(filledOrders.stream(), rejectedOrders.stream())
                                           .collect(Collectors.toList())));
            ((AbstractTableModel) ordersStatsTable.getModel())
                    .fireTableDataChanged();
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    public JTable getFilledOrdersTable() {
        return filledOrdersTable;
    }

    public JTable getRejectedOrdersTable() {
        return rejectedOrdersTable;
    }

    private JTable buildOrdersTable(final List<Order> orders, final boolean filled) {
        // local inner class
        final JTable ordersTable = new JTable(filled ? new FilledOrdersTableModel(orders)
                                                      : new RejectedOrdersTableModel(orders)) {
            private static final long serialVersionUID = 51L;

            public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {
                final Component component = super.prepareRenderer(renderer, row, column);
                switch ((OrderSide) getModel().getValueAt(convertRowIndexToModel(row), 3)) {
                    case BUY:
                        component.setBackground(Color.BLACK);
                        component.setForeground(Color.WHITE);
                        break;
                    case SELL:
                        component.setBackground(Color.RED);
                        component.setForeground(Color.YELLOW);
                        break;
                    default:
                        break;
                }
                return component;
            }
        };

        ordersTable.getColumnModel()
                   .getColumn(filled ? 9 : 8)
                   .setCellRenderer(new DatetimeTableCellRenderer());

        // pre-set sorter enabled on the FilledDate column.
        SwingUtils.setTableSorter(ordersTable, filled ? 9 : 8, SortOrder.DESCENDING);
        ordersTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        ordersTable.setFillsViewportHeight(true);

        return ordersTable;
    }

    @Override
    public void cleanUp() {
        SimulatorUtils.shutdownExecutorService(es, 5L, TimeUnit.SECONDS);
    }

}
