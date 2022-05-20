package me.saif.betterenderchests.data.database;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Database {

    public abstract Connection getConnection() throws SQLException;

    public abstract void close();

    public abstract String getDatabaseName();

}
