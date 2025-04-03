package me.saif.betterenderchests.data;

import me.saif.betterenderchests.data.database.SQLiteDatabase;
import me.saif.betterenderchests.utils.TimeUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class SQLiteDataManager extends SQLDataManager {

    public SQLiteDataManager(SQLiteDatabase database) {
        super(database);
    }

    @Override
    public boolean createBackup(Logger logger, File backupsFolder) {
        if (!backupsFolder.isDirectory()) {
            backupsFolder.mkdirs();
        }
        try (Connection connection = this.database.getConnection();
             Statement statement = connection.createStatement()) {
            logger.info("Creating backup of data.");
            File backupFile = new File(backupsFolder, "sqlitebackup-" + TimeUtils.getCurrentFormattedTime() + ".db");
            statement.executeUpdate("backup to " + backupFile);
            logger.info("Finished backing up to " + backupFile.getName());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
