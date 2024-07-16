package com.raymondpang365.utility.database;

import java.sql.Connection;

public interface DatabaseConnection extends AutoCloseable {
    Connection getConnection();
}
