package me.saif.betterenderchests.converters;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.data.SQLDataManager;
import me.saif.betterenderchests.data.database.MySQLDatabase;
import me.saif.betterenderchests.data.database.SQLDatabase;
import me.saif.betterenderchests.data.database.SQLiteDatabase;
import me.saif.betterenderchests.enderchest.EnderChestSnapshot;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SQLiteMySQLConverter extends Converter {

    private static final int BATCH_AMOUNT = 100;

    public SQLiteMySQLConverter(VariableEnderChests plugin) {
        super(plugin, "SQLiteMySQLConverter");
    }

    @Override
    public boolean convert() {
        if (Bukkit.getOnlinePlayers().size() > 0)
            throw new IllegalStateException("Cannot convert with players online");

        if (!(plugin.getSQLDatabase() instanceof SQLiteDatabase))
            throw new IllegalStateException("Current database is not SQLite");

        SQLDatabase mySQLDB;
        try {
            mySQLDB = new MySQLDatabase(
                    this.plugin.getConfig().getString("database.host"),
                    this.plugin.getConfig().getInt("database.port"),
                    this.plugin.getConfig().getString("database.database"),
                    this.plugin.getConfig().getString("database.username"),
                    this.plugin.getConfig().getString("database.password"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        SQLDataManager sqlDataManager = new SQLDataManager(this.plugin, mySQLDB);

        try (Connection connection = mySQLDB.getConnection();
             Statement statement = connection.createStatement();) {
            statement.executeUpdate("DROP TABLE " + sqlDataManager.getDataTableName() + ";");

            sqlDataManager.init();

            Set<UUID> allOld = plugin.getDataManager().getAllEnderChests();

            Set<UUID> uuids = new HashSet<>();

            for (UUID uuid : allOld) {
                uuids.add(uuid);

                if (uuids.size() != BATCH_AMOUNT)
                    continue;

                migrate(uuids, this.plugin.getDataManager(), sqlDataManager);
                uuids.clear();
            }

            if (uuids.size() != 0) {
                migrate(uuids, this.plugin.getDataManager(), sqlDataManager);
                uuids.clear();
            }

            this.plugin.getLogger().info("Migration Complete!");
            this.plugin.getLogger().info("Please restart your server!");
            this.plugin.getConfig().set("database.mysql", true);
            this.plugin.saveConfig();

            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> Bukkit.getPluginManager().disablePlugin(this.plugin));
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void migrate(Set<UUID> uuids, DataManager oldDataManager, SQLDataManager newDataManager) {
        Map<UUID, EnderChestSnapshot> snapshotMap = oldDataManager.loadEnderChestsByUUID(uuids);

        newDataManager.saveEnderChestMultiple(snapshotMap);

        Map<String, UUID> nameMap = new HashMap<>();

        snapshotMap.forEach((uuid, enderChestSnapshot) -> nameMap.put(enderChestSnapshot.getName(), uuid));

        newDataManager.saveNameAndUUIDs(nameMap);

        this.plugin.getLogger().info("Migrated data for " + uuids);
    }
}
