package me.saif.betterenderchests.data.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SQLiteDatabase extends Database{

    private final File file;
    private Connection connection;

    public SQLiteDatabase(File file) throws IOException {
        this.file = file;
        if (!this.file.getParentFile().isDirectory()) {
            if (!this.file.getParentFile().mkdirs()) {
                throw new UnsupportedOperationException("Could not create the following directory: " + this.file.getParentFile().getAbsolutePath());
            }
        }
        try {
            if (!this.file.exists())
                this.file.createNewFile();
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.file);
            Logger.getGlobal().info("Opened database at " + this.file + " successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDatabaseName() {
        return String.valueOf(file);
    }
}
