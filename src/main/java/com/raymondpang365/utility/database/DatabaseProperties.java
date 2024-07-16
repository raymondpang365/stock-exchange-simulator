package com.raymondpang365.utility.database;

public final class DatabaseProperties {
    private final String host;
    private final int port;
    private final String databaseName;
    private String userName;
    private String password;

    public DatabaseProperties(final String host,
                              final int port,
                              final String databaseName,
                              final String userName,
                              final String password) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.userName = userName;
        this.password = password;
    }

    public DatabaseProperties(final String host,
                              final int port,
                              final String databaseName) {
        this(host, port, databaseName, null, null);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}
