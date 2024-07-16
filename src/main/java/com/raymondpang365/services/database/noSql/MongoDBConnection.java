package com.raymondpang365.services.database.noSql;

import com.raymondpang365.utility.database.DatabaseProperties;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBConnection implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBConnection.class);

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;

    public MongoDBConnection(final DatabaseProperties databaseProperties) {
        mongoClient = new MongoClient(databaseProperties.getHost(), databaseProperties.getPort());
        mongoDatabase = mongoClient.getDatabase(databaseProperties.getDatabaseName());
    }

    @Override
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("Connection closed.");
        }
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }
}
