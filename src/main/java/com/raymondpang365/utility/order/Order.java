package com.raymondpang365.utility.order;

import quickfix.SessionID;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Order implements Serializable {

    private static final long serialVersionUID = 47L;

    private String id;
    private SessionID sessionID;
    private String symbol;
    private int quantity;
    private int open;
    private int executed;
    private OrderSide side = OrderSide.BUY;
    private OrderType type = OrderType.MARKET;
    private OrderTimeInForce timeInForce = OrderTimeInForce.DAY;
    private double limitPrice;
    private double avgPx;
    private boolean rejected;
    private boolean canceled;
    private boolean isNew = true;
    private String message;
    private String originalID;
    private boolean creditCheckFailed;
    private LocalDateTime storeDate;
    private String marketDataID;

    public Order() {
        id = UUID.randomUUID().toString();
    }

    public Order(final String id) {
        this.id = id;
    }

    /**
     * This constructor is used when the order gets retrieved from the back-end.
     */
    public Order(final String id,
                 final String symbol,
                 final int quantity,
                 final OrderSide side,
                 final OrderType type,
                 final OrderTimeInForce timeInForce,
                 final double limitPrice,
                 final double avgPx,
                 final String originalID,
                 final LocalDateTime storeDate,
                 final boolean rejected,
                 final boolean creditCheckFailed) {
        this.id = id;
        this.symbol = symbol;
        this.quantity = quantity;
        this.side = side;
        this.type = type;
        this.timeInForce = timeInForce;
        this.limitPrice = limitPrice;
        this.avgPx = avgPx;
        this.originalID = originalID;
        this.storeDate = storeDate;
        this.rejected = rejected;
        this.creditCheckFailed = creditCheckFailed;
    }

    public Order(final String id,
                 final String symbol,
                 final int quantity,
                 final OrderSide side,
                 final OrderType type,
                 final OrderTimeInForce timeInForce,
                 final double limitPrice,
                 final double avgPx,
                 final String originalID,
                 final LocalDateTime storeDate,
                 final boolean rejected,
                 final String marketDataID,
                 final boolean creditCheckFailed) {
        this(id, symbol, quantity, side, type, timeInForce, limitPrice, avgPx, originalID, storeDate, rejected,
             creditCheckFailed);
        this.marketDataID = marketDataID;
    }

    public SessionID getSessionID() {
        return sessionID;
    }

    public void setSessionID(final SessionID sessionID) {
        this.sessionID = sessionID;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(final int open) {
        this.open = open;
    }

    public int getExecuted() {
        return executed;
    }

    public void setExecuted(final int executed) {
        this.executed = executed;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(final OrderSide side) {
        this.side = side;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(final OrderType type) {
        this.type = type;
    }

    public OrderTimeInForce getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(final OrderTimeInForce timeInForce) {
        this.timeInForce = timeInForce;
    }

    public double getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(final double limitPrice) {
        this.limitPrice = limitPrice;
    }

    public double getAvgPx() {
        return avgPx;
    }

    public void setAvgPx(final double avgPx) {
        this.avgPx = avgPx;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(final boolean rejected) {
        this.rejected = rejected;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(final boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(final boolean aNew) {
        isNew = aNew;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getOriginalID() {
        return originalID;
    }

    public void setOriginalID(final String originalID) {
        this.originalID = originalID;
    }

    public boolean isCreditCheckFailed() {
        return creditCheckFailed;
    }

    public void setCreditCheckFailed(final boolean creditCheckFailed) {
        this.creditCheckFailed = creditCheckFailed;
    }

    public LocalDateTime getStoreDate() {
        return storeDate;
    }

    public void setStoreDate(final LocalDateTime storeDate) {
        this.storeDate = storeDate;
    }

    public String getMarketDataID() {
        return marketDataID;
    }

    public void setMarketDataID(final String marketDataID) {
        this.marketDataID = marketDataID;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Order order = (Order) o;
        return quantity == order.quantity && open == order.open && executed == order.executed
                && Double.compare(limitPrice, order.limitPrice) == 0 && Double.compare(avgPx, order.avgPx) == 0
                && rejected == order.rejected && canceled == order.canceled && isNew == order.isNew
                && creditCheckFailed == order.creditCheckFailed && Objects.equals(sessionID, order.sessionID)
                && Objects.equals(symbol, order.symbol) && side == order.side && type == order.type
                && timeInForce == order.timeInForce && Objects.equals(message, order.message)
                && Objects.equals(id, order.id) && Objects.equals(originalID, order.originalID)
                && Objects.equals(storeDate, order.storeDate) && Objects.equals(marketDataID, order.marketDataID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionID, symbol, quantity, open, executed, side, type, timeInForce, limitPrice, avgPx,
                            rejected, canceled, isNew, message, id, originalID, creditCheckFailed, storeDate, marketDataID);
    }

    @Override
    public String toString() {
        return "Order{" +
                "sessionID=" + sessionID +
                ", symbol='" + symbol + '\'' +
                ", quantity=" + quantity +
                ", open=" + open +
                ", executed=" + executed +
                ", side=" + side +
                ", type=" + type +
                ", timeInForce=" + timeInForce +
                ", limitPrice=" + limitPrice +
                ", avgPx=" + avgPx +
                ", rejected=" + rejected +
                ", canceled=" + canceled +
                ", isNew=" + isNew +
                ", message='" + message + '\'' +
                ", originalID='" + originalID + '\'' +
                ", creditCheckFailed=" + creditCheckFailed +
                ", storeDate=" + storeDate +
                ", marketDataID='" + marketDataID + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
