package com.raymondpang365.ui.components;

import com.raymondpang365.ui.model.MarketDataSummary;
import com.raymondpang365.ui.model.MarketDataSummaryTableModel;
import com.raymondpang365.ui.model.MarketDataTableModel;
import com.raymondpang365.utility.marketData.MarketData;
import com.raymondpang365.utility.utils.SimulatorUtils;
import com.raymondpang365.utility.utils.SwingUtils;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Market data panel with a table as main content.
 */
public final class MarketDataPanel extends JPanel implements PanelCleanUp {

    private static final long serialVersionUID = 23L;

    private final List<MarketData> marketDataItems;
    private final List<MarketDataSummary> marketDataSummaryItems = new ArrayList<>();
    private final JTable marketDataTable;
    private final JTable marketDataSummaryTable;
    private final ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();

    public MarketDataPanel(final List<MarketData> marketDataItems) {
        super(new BorderLayout(10, 20));
        this.marketDataItems = marketDataItems;
        buildMarketDataSummary();
        marketDataTable = buildMarketDataTable(false);
        marketDataSummaryTable = buildMarketDataTable(true);

        final JScrollPane marketDataSummaryScrollPanel = new JScrollPane(marketDataSummaryTable);
        marketDataSummaryScrollPanel.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Market Data Summary"));
        add(marketDataSummaryScrollPanel, BorderLayout.NORTH);

        final JScrollPane marketDataScrollPanel = new JScrollPane(marketDataTable);
        marketDataScrollPanel.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Market Data"));
        add(marketDataScrollPanel, BorderLayout.CENTER);

        es.scheduleWithFixedDelay(() -> {
            buildMarketDataSummary();
            ((AbstractTableModel) marketDataSummaryTable.getModel()).fireTableDataChanged();
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    public JTable getMarketDataTable() {
        return marketDataTable;
    }

    private void buildMarketDataSummary() {
        marketDataSummaryItems.clear();
        final ArrayList<MarketData> marketDataItemsCopy = new ArrayList<>(marketDataItems);
        final Map<String, Long> itemsNumber = marketDataItemsCopy
                .parallelStream()
                .collect(Collectors.groupingBy(MarketData::getSymbol,
                                               Collectors.counting()));

        final Map<String, Double> avgBid = marketDataItemsCopy
                .parallelStream()
                .collect(Collectors.groupingBy(MarketData::getSymbol,
                                               Collectors.averagingDouble(MarketData::getBid)));

        final Map<String, Double> avgAsk = marketDataItemsCopy
                .parallelStream()
                .collect(Collectors.groupingBy(MarketData::getSymbol,
                                               Collectors.averagingDouble(MarketData::getAsk)));

        final Map<String, Double> avgBidSize = marketDataItemsCopy
                .parallelStream()
                .collect(Collectors.groupingBy(MarketData::getSymbol,
                                               Collectors.averagingDouble(MarketData::getBidSize)));

        final Map<String, Double> avgAskSize = marketDataItemsCopy
                .parallelStream()
                .collect(Collectors.groupingBy(MarketData::getSymbol,
                                               Collectors.averagingDouble(MarketData::getAskSize)));

        itemsNumber.forEach((key, val) -> marketDataSummaryItems.add(
                new MarketDataSummary(key,
                                      avgBid.get(key),
                                      avgAsk.get(key),
                                      avgBidSize.get(key),
                                      avgAskSize.get(key),
                                      val)));
    }

    private JTable buildMarketDataTable(final boolean isSummaryTable) {
        // inner local class
        final JTable marketDataTable = new JTable(isSummaryTable ?
                                                          new MarketDataSummaryTableModel(marketDataSummaryItems)
                                                          : new MarketDataTableModel(marketDataItems)) {
            private static final long serialVersionUID = 19L;

            public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {
                final Component component = super.prepareRenderer(renderer, row, column);
                if (row % 2 == 0) {
                    component.setBackground(Color.WHITE);
                    component.setForeground(Color.BLACK);
                } else {
                    component.setBackground(Color.GREEN);
                    component.setForeground(Color.RED);
                }
                return component;
            }
        };

        if (isSummaryTable) {
            SwingUtils.setTableSorter(marketDataTable, 0, SortOrder.ASCENDING);
        } else {//sort on quote time --> column 6.
            marketDataTable.getColumnModel()
                           .getColumn(6)
                           .setCellRenderer(new DatetimeTableCellRenderer());
            SwingUtils.setTableSorter(marketDataTable, 6, SortOrder.DESCENDING);
        }
        marketDataTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        marketDataTable.setFillsViewportHeight(true);
        return marketDataTable;
    }

    @Override
    public void cleanUp() {
        SimulatorUtils.shutdownExecutorService(es, 5L, TimeUnit.SECONDS);
    }

}
