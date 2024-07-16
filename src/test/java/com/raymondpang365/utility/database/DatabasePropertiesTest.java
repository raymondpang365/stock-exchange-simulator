package com.raymondpang365.utility.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class DatabasePropertiesTest {

    @ParameterizedTest
    @CsvSource({
            "host1, 101, myDatabase1, userId1, pwd1",
            "host2, 102, myDatabase2, userId2, pwd2"
    })
    @DisplayName("Test DatabaseProperties constructor")
    void testDatabasePropertiesConstructor(final String host,
                                           final int port,
                                           final String databaseName,
                                           final String userName,
                                           final String password) {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName, userName, password);
        assertNotNull(databaseProperties);
        System.out.println(databaseProperties);

        assertEquals(host, databaseProperties.getHost());
        assertEquals(port, databaseProperties.getPort());
        assertEquals(databaseName, databaseProperties.getDatabaseName());
        assertEquals(userName, databaseProperties.getUserName());
        assertEquals(password, databaseProperties.getPassword());
    }

    @ParameterizedTest
    @CsvSource({
            "host1, 101, myDatabase1",
            "host2, 102, myDatabase2"
    })
    @DisplayName("Test DatabaseProperties constructor chaining")
    void testDatabasePropertiesConstructorChaining(final String host,
                                                   final int port,
                                                   final String databaseName) {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName);
        assertNotNull(databaseProperties);
        System.out.println(databaseProperties);

        assertEquals(host, databaseProperties.getHost());
        assertEquals(port, databaseProperties.getPort());
        assertEquals(databaseName, databaseProperties.getDatabaseName());

        assertNull(databaseProperties.getUserName());
        assertNull(databaseProperties.getPassword());
    }

}
