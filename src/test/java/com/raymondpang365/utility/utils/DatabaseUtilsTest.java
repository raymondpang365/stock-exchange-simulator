package com.raymondpang365.utility.utils;

import com.raymondpang365.utility.database.DatabaseProperties;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DatabaseUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "host1, 101, myDatabase1, userId1, pwd1",
            "host2, 102, myDatabase2, userId2, pwd2"
    })
    @DisplayName("Test MySqlConnection connection string")
    void testMySqlConnectionString(final String host,
                                   final int port,
                                   final String databaseName,
                                   final String userName,
                                   final String password) {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName, userName, password);
        assertNotNull(databaseProperties);

        assertEquals(String.format("jdbc:mysql://%s:%d/%s", host, port, databaseName),
                     DatabaseUtils.getMySqlConnectionUrl(databaseProperties));
    }

    @ParameterizedTest
    @CsvSource({
            "host1, 101, myDatabase1, userId1, pwd1, 3",
            "host2, 102, myDatabase2, userId2, pwd2, 5"
    })
    @DisplayName("Test Database connection pool")
    void testDatabaseConnectionPool(final String host,
                                    final int port,
                                    final String databaseName,
                                    final String userName,
                                    final String password,
                                    final int poolSize) {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName, userName, password);
        assertNotNull(databaseProperties);

        final BasicDataSource dataSource = DatabaseUtils.getDataSource(databaseProperties, poolSize);
        assertNotNull(dataSource);
        assertEquals(String.format("jdbc:mysql://%s:%d/%s", host, port, databaseName),
                     dataSource.getUrl());
        assertEquals(poolSize, dataSource.getInitialSize());
        assertEquals(poolSize, dataSource.getMaxTotal());
    }

}
