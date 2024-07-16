package com.raymondpang365.utility.database.creditCheck;

import com.raymondpang365.utility.database.DatabaseProperties;
import com.raymondpang365.utility.utils.DatabaseUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class CreditCheckTest {

    @ParameterizedTest
    @CsvSource({
            "localhost, 3306, TradingMachine, devuser, Atging@123"
    })
    @DisplayName("Test setCredit Stored Procedure")
    @Disabled
    void testSetCredit(final String host,
                       final int port,
                       final String databaseName,
                       final String userName,
                       final String password) throws SQLException {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName, userName, password);
        assertNotNull(databaseProperties);

        try (final BasicDataSource creditCheckConnectionPool = DatabaseUtils.getDataSource(databaseProperties, 10);
             final Connection mySqlConnection = creditCheckConnectionPool.getConnection()) {
            final CreditCheck creditCheck = new CreditCheck(mySqlConnection);
            try {
                creditCheck.setCredit(1000D);
            } finally {
                creditCheck.closeConnection();
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
            "localhost, 3306, TradingMachine, devuser, Atging@123"
    })
    @DisplayName("Test hasEnoughCredit Stored Procedure")
    @Disabled
    void testHasEnoughCredit(final String host,
                             final int port,
                             final String databaseName,
                             final String userName,
                             final String password) throws SQLException {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName, userName, password);
        assertNotNull(databaseProperties);

        try (final BasicDataSource creditCheckConnectionPool = DatabaseUtils.getDataSource(databaseProperties, 10);
             final Connection mySqlConnection = creditCheckConnectionPool.getConnection()) {
            final CreditCheck creditCheck = new CreditCheck(mySqlConnection);
            try {
                assertTrue(creditCheck.hasEnoughCredit(100D));
                assertFalse(creditCheck.hasEnoughCredit(10000D));
            } finally {
                creditCheck.closeConnection();
            }
        }
    }

}
