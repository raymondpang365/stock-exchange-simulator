package com.raymondpang365.utility.utils;

import com.raymondpang365.utility.database.DatabaseProperties;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.apache.commons.dbcp2.BasicDataSource;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class DatabaseUtils {
    private DatabaseUtils() {
    }

    public static String getMySqlConnectionUrl(final DatabaseProperties dbProperties) {
        return String.format("jdbc:mysql://%s:%d/%s",
                             dbProperties.getHost(),
                             dbProperties.getPort(),
                             dbProperties.getDatabaseName());
    }

    /**
     * Creates a database connection pool.
     *
     * @param dbProperties
     * @param poolSize
     * @return
     */
    public static BasicDataSource getDataSource(final DatabaseProperties dbProperties, final int poolSize) {
        final BasicDataSource ds = new BasicDataSource();
        ds.setUrl(getMySqlConnectionUrl(dbProperties));
        ds.setUsername(dbProperties.getUserName());
        ds.setPassword(dbProperties.getPassword());
        ds.setInitialSize(poolSize);
        ds.setMaxTotal(poolSize);
        return ds;
    }

    public static MysqlDataSource getMysqlDataSource(final DatabaseProperties dbProperties) {
        final MysqlDataSource ds = new MysqlDataSource();
        ds.setUrl(getMySqlConnectionUrl(dbProperties));
        ds.setUser(dbProperties.getUserName());
        ds.setPassword(dbProperties.getPassword());
        return ds;
    }

    public static LocalDateTime convertDateToLocalDateTime(final Date dateToConvert) {
        return dateToConvert.toInstant()
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDateTime();
    }

    public static Date convertSqlDateToUtilDate(final java.sql.Date sqlDate) {
        return new Date(sqlDate.getTime());
    }
}
