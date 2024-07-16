package com.raymondpang365.services.database.noSql;

import com.raymondpang365.services.database.DataManager;
import com.raymondpang365.utility.marketData.MarketData;
import com.raymondpang365.utility.order.Order;
import com.raymondpang365.utility.order.OrderSide;
import com.raymondpang365.utility.order.OrderTimeInForce;
import com.raymondpang365.utility.order.OrderType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.raymondpang365.utility.utils.DatabaseUtils.convertDateToLocalDateTime;

public final class MongoDBManager implements DataManager {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBManager.class);

    private final MongoDBConnection mongoDBConnection;
    private final MongoCollection<Document> executedOrdersCollection;
    private final MongoCollection<Document> marketDataCollection;

    public MongoDBManager(final MongoDBConnection mongoDBConnection,
                          final String executedOrdersCollectionName,
                          final String marketDataCollectionName) {
        this.mongoDBConnection = mongoDBConnection;
        executedOrdersCollection = mongoDBConnection.getMongoDatabase().getCollection(executedOrdersCollectionName);
        marketDataCollection = marketDataCollectionName == null ?
                null : mongoDBConnection.getMongoDatabase().getCollection(marketDataCollectionName);
    }

    public MongoDBManager(final MongoDBConnection mongoDBConnection, final String executedOrdersCollectionName) {
        this(mongoDBConnection, executedOrdersCollectionName, null);
    }

    @Override
    public void storeOrder(final Order order) {
        executedOrdersCollection.replaceOne(new Document("FilledOrder", order.getId()),
                                            convertOrderToBsonDocument(order),
                                            new UpdateOptions().upsert(true));
        logger.debug("{} added to collection: {}", order, executedOrdersCollection);
    }

    @Override
    public List<Order> getOrders(final Optional<OrderType> orderType) {
        final Instant start = Instant.now();
        final List<Order> result = new ArrayList<>();
        try (final MongoCursor<Document> cursor = orderType.map(type ->
                                                                        executedOrdersCollection
                                                                                .find(new Document("Type", type.toString()))
                                                                                .iterator())
                                                           .orElseGet(() -> executedOrdersCollection
                                                                   .find()
                                                                   .iterator())) {
            while (cursor.hasNext()) {
                final Document doc = cursor.next();
                result.add(new Order(doc.getString("ID"),
                                     doc.getString("Symbol"),
                                     doc.getInteger("Quantity"),
                                     OrderSide.fromString(doc.getString("Side")),
                                     OrderType.fromString(doc.getString("Type")),
                                     OrderTimeInForce.fromString(doc.getString("TimeInForce")),
                                     doc.getDouble("LimitPrice"),
                                     doc.getDouble("AvgPrice"),
                                     doc.getString("OriginalID"),
                                     convertDateToLocalDateTime(doc.getDate("StoreDate")),
                                     doc.getBoolean("IsRejected"),
                                     doc.getString("MarketDataID"),
                                     doc.getBoolean("IsCreditCheckFailed", false)));
            }
        }
        final long timeElapsed = Duration.between(start, Instant.now()).toMillis();
        logger.info("Time taken to retrieve orders: {} ms.", timeElapsed);
        return result;
    }

    @Override
    public void storeMarketDataItems(final List<MarketData> marketDataItems, final boolean deleteFirst) {
        logger.debug("Starting to store {} MarketData items...", marketDataItems.size());
        if (deleteFirst) {
            marketDataCollection.deleteMany(new Document());
        }
        final List<Document> docs = new ArrayList<>();
        marketDataItems.forEach(marketDataItem -> docs.add(convertMarketDataToBsonDocument(marketDataItem)));
        marketDataCollection.insertMany(docs);
        logger.debug("Data stored successfully");
    }

    @Override
    public List<MarketData> getMarketData(final Optional<String> symbol) {
        final long startTime = System.currentTimeMillis();
        final List<MarketData> result = new ArrayList<>();
        try (final MongoCursor<Document> cursor = symbol.map(s -> marketDataCollection
                                                                .find(new Document("Symbol", s))
                                                                .iterator())
                                                        .orElseGet(() -> marketDataCollection
                                                                .find()
                                                                .iterator())) {
            while (cursor.hasNext()) {
                final Document doc = cursor.next();
                result.add(new MarketData(doc.getString("ID"),
                                          doc.getString("Symbol"),
                                          doc.getDouble("Ask"),
                                          doc.getDouble("Bid"),
                                          doc.getInteger("AskSize"),
                                          doc.getInteger("BidSize"),
                                          convertDateToLocalDateTime(doc.getDate("QuoteTime"))));
            }
        }
        logger.info("Time taken to retrieve orders: " + (startTime - System.currentTimeMillis()) + " ms.");
        return result;
    }

    @Override
    public void close() {
        mongoDBConnection.close();
    }

    private static Document convertOrderToBsonDocument(final Order order) {
        return new Document("ID", order.getId())
                .append("Symbol", order.getSymbol())
                .append("Quantity", order.getQuantity())
                .append("Side", order.getSide().toString())
                .append("Type", order.getType().toString())
                .append("TimeInForce", order.getTimeInForce().toString())
                .append("LimitPrice", order.getLimitPrice())
                .append("AvgPrice", order.getAvgPx())
                .append("OriginalID", order.getOriginalID())
                .append("StoreDate", new Date())
                .append("IsRejected", order.isRejected())
                .append("MarketDataID", order.getMarketDataID())
                .append("IsCreditCheckFailed", order.isCreditCheckFailed());
    }

    private static Document convertMarketDataToBsonDocument(final MarketData marketData) {
        return new Document("ID", marketData.getId()).
                append("Symbol", marketData.getSymbol())
                .append("Ask", marketData.getAsk())
                .append("Bid", marketData.getBid())
                .append("AskSize", marketData.getAskSize())
                .append("BidSize", marketData.getBidSize())
                .append("QuoteTime", marketData.getQuoteDateTime());
    }
}
