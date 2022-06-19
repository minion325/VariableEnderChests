package me.saif.betterenderchests.data.database;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class SQLDatabase {

    protected final HikariDataSource dataSource;

    public SQLDatabase() {
        this.dataSource = new HikariDataSource();
    }

    public Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isConnected() {
        try (Connection connection = this.getConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        if (!dataSource.isClosed())
            this.dataSource.close();
    }

    public abstract String getType();

}
