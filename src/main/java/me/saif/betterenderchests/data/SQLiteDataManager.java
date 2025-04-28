package me.saif.betterenderchests.data;

import me.saif.betterenderchests.data.database.SQLiteDatabase;
import me.saif.betterenderchests.enderchest.EnderChestSnapshot;
import me.saif.betterenderchests.utils.TimeUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class SQLiteDataManager extends SQLDataManager {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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

    @Override
    public void saveNameAndUUIDs(Map<String, UUID> map) {
        this.executeOnSingleThread(() -> super.saveNameAndUUIDs(map));
    }

    @Override
    public void saveNameAndUUID(String name, UUID uuid) {
        this.executeOnSingleThread(() -> super.saveNameAndUUID(name, uuid));
    }

    @Override
    public void saveEnderChestMultiple(Map<UUID, EnderChestSnapshot> snapshotMap) {
        this.executeOnSingleThread(() -> super.saveEnderChestMultiple(snapshotMap));
    }

    @Override
    public void purge(char... confirm) {
        this.executeOnSingleThread(() -> super.purge(confirm));
    }

    @Override
    public void deleteEnderChest(String name) {
        this.executeOnSingleThread(() -> super.deleteEnderChest(name));
    }

    @Override
    public void deleteEnderChest(UUID uuid) {
        this.executeOnSingleThread(() -> super.deleteEnderChest(uuid));
    }

    @Override
    public void saveEnderChest(UUID uuid, EnderChestSnapshot snapshot) {
        this.executeOnSingleThread(() -> super.saveEnderChest(uuid, snapshot));
    }

    private void executeOnSingleThread(Runnable runnable) {
        try {
            executorService.submit(runnable).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void finishUp() {
        super.finishUp();
        this.executorService.shutdown();
    }
}
