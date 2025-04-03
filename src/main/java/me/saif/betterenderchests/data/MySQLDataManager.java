package me.saif.betterenderchests.data;

import me.saif.betterenderchests.data.database.MySQLDatabase;

import java.io.File;
import java.util.logging.Logger;

public class MySQLDataManager extends SQLDataManager {

    public MySQLDataManager(MySQLDatabase database) {
        super(database);
    }

    @Override
    public boolean createBackup(Logger logger, File backupsFolder) {
        logger.info("Backup is not supported for this database type.");
        return false;
    }
}
