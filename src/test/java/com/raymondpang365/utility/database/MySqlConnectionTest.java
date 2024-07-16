package com.raymondpang365.utility.database;

import com.raymondpang365.utility.utils.DatabaseUtils;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlConnectionTest {

    @ParameterizedTest
    @CsvSource({
            "localhost, 3306, world"
    })
    @DisplayName("Test basic MySQL connection using Swing")
    @Disabled
    void testMySqlConnectionUsingSwing(final String host,
                                       final int port,
                                       final String databaseName) {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName);
        assertNotNull(databaseProperties);

        // userName
        final String userName = getUserName();
        databaseProperties.setUserName(userName);

        // password
        final String password = getPassword();
        databaseProperties.setPassword(password);

        try (final MySqlConnection mySqlConnection = new MySqlConnection((databaseProperties))) {
            assertNotNull(mySqlConnection);
            assertNotNull(mySqlConnection.getConnection());
            assertEquals(String.format("jdbc:mysql://%s:%d/%s", host, port, databaseName),
                         mySqlConnection.getConnectionString());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "localhost, 3306, world"
    })
    @DisplayName("Test basic MySQL connection using DataSource")
    @Disabled
    void testMySqlConnectionUsingDataSource(final String host,
                                            final int port,
                                            final String databaseName) {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName);
        assertNotNull(databaseProperties);

        // userName
        final String userName = getUserName();
        databaseProperties.setUserName(userName);

        // password
        final String password = getPassword();
        databaseProperties.setPassword(password);

        final MysqlDataSource dataSource = DatabaseUtils.getMysqlDataSource(databaseProperties);
        assertEquals(DatabaseUtils.getMySqlConnectionUrl(databaseProperties), dataSource.getUrl());

        try (final Connection mySqlConnection = dataSource.getConnection()) {
            assertNotNull(mySqlConnection);
            assertTrue(mySqlConnection.getAutoCommit());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "localhost, 3306, world"
    })
    @DisplayName("Test querying data using MySQL connection")
    @Disabled
    void testQueryingData(final String host,
                          final int port,
                          final String databaseName) {
        final DatabaseProperties databaseProperties
                = new DatabaseProperties(host, port, databaseName);
        assertNotNull(databaseProperties);

        // userName
        final String userName = getUserName();
        databaseProperties.setUserName(userName);

        // password
        final String password = getPassword();
        databaseProperties.setPassword(password);

        final MysqlDataSource dataSource = DatabaseUtils.getMysqlDataSource(databaseProperties);
        assertEquals(DatabaseUtils.getMySqlConnectionUrl(databaseProperties), dataSource.getUrl());

        try (final Connection mySqlConnection = dataSource.getConnection();
             final Statement statement = mySqlConnection.createStatement()) {
            assertNotNull(statement);

            final String tableName = "city";
            final String filterColumnName = "CountryCode";
            final String filterColumnValue = "NLD";

            //final String sqlQuery = String.format("SELECT * FROM %s.%s", databaseName, tableName);
            final String sqlQuery = String.format("SELECT * FROM %s.%s WHERE %s = '%s'",
                                                  databaseName,
                                                  tableName,
                                                  filterColumnName,
                                                  filterColumnValue);

            final ResultSet resultSet = statement.executeQuery(sqlQuery);
            assertNotNull(resultSet);

            final ResultSetMetaData metaData = resultSet.getMetaData();
            assertNotNull(metaData);
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.printf("%d %s %s%n",
                                  i,
                                  metaData.getColumnName(i),
                                  metaData.getColumnTypeName(i));
            }
            System.out.println("===============================");

            // headers
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.printf("%-20s",
                                  metaData.getColumnName(i).toUpperCase());
            }
            System.out.println();

            // values
            while (resultSet.next()) {
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    System.out.printf("%-20s",
                                      resultSet.getString(i));
                }
                System.out.println();
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getUserName() {
        final String userName = JOptionPane.showInputDialog(null,
                                                            "Enter DB Username");
        assertNotNull(userName);

        return userName.trim();
    }

    private static String getPassword() {
        final JPasswordField pf = new JPasswordField();
        final int okCxl = JOptionPane.showConfirmDialog(null,
                                                        pf,
                                                        "Enter DB password",
                                                        JOptionPane.OK_CANCEL_OPTION);
        final char[] password = (okCxl == JOptionPane.OK_OPTION) ? pf.getPassword() : null;
        assertNotNull(password);

        final String passwordStr = String.valueOf(password).trim();
        Arrays.fill(password, ' ');

        return passwordStr;
    }

}
