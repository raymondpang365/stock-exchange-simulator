package com.raymondpang365.utility.database;

import com.raymondpang365.utility.utils.DatabaseUtils;

import java.sql.SQLException;

public final class MySqlConnection extends AbstractDatabaseConnection {
    public MySqlConnection(final DatabaseProperties databaseProperties) throws SQLException {
        super(databaseProperties);
    }

    @Override
    protected String getConnectionString() {
        return DatabaseUtils.getMySqlConnectionUrl(databaseProperties);
    }
}
