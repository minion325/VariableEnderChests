package me.saif.betterenderchests.data;

import me.saif.betterenderchests.BetterEnderChests;
import me.saif.betterenderchests.data.database.Database;
import me.saif.betterenderchests.data.database.SQLiteDatabase;
import me.saif.betterenderchests.enderchest.EnderChestSnapshot;
import me.saif.betterenderchests.utils.ItemStackSerializer;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class SQLiteDataManager extends DataManager {

    private Database database;
    private String dataTableName = "enderchests";
    private String playersTableName = "players";

    public SQLiteDataManager(BetterEnderChests plugin) {
        super(plugin);
        try {
            this.database = new SQLiteDatabase(new File(getPlugin().getDataFolder(), "data.db"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPlayersTableName() {
        return playersTableName;
    }

    public String getDataTableName() {
        return dataTableName;
    }

    @Override
    public void init() {
        String createDataTable = "CREATE TABLE IF NOT EXISTS " + getDataTableName() + " (UUID VARCHAR(36) NOT NULL PRIMARY KEY, ROWS INT, CONTENTS MEDIUMTEXT);";
        String createPlayersTable = "CREATE TABLE IF NOT EXISTS " + getPlayersTableName() + " (UUID VARCHAR(36) NOT NULL UNIQUE, NAME VARCHAR(16) NOT NULL UNIQUE);";
        try (Statement statement = this.database.getConnection().createStatement()) {
            statement.executeUpdate(createDataTable);
            statement.executeUpdate(createPlayersTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finishUp() {
        this.database.close();
    }

    @Override
    public void saveNameAndUUID(String name, UUID uuid) {
        String sql = "REPLACE INTO " + getPlayersTableName() + " (UUID,NAME) VALUES (?, ?)";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveEnderChestMultiple(Map<UUID, EnderChestSnapshot> snapshotMap) {
        String sql = "REPLACE INTO " + getDataTableName() + " (UUID,ROWS,CONTENTS) VALUES (?,?,?)";
        try (PreparedStatement statement = this.database.getConnection().prepareStatement(sql)) {
            for (UUID uuid : snapshotMap.keySet()) {
                EnderChestSnapshot snapshot = snapshotMap.get(uuid);
                statement.setString(1, uuid.toString());
                statement.setInt(2, snapshot.getRows());
                statement.setString(3, ItemStackSerializer.serialize(snapshot.getContents()));
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<UUID, EnderChestSnapshot> loadEnderChestsByUUID(Set<UUID> uuids) {
        if (uuids.isEmpty())
            return new HashMap<>();
        String sql = "SELECT " + getDataTableName() + ".UUID," + getPlayersTableName() + ".NAME,ROWS,CONTENTS FROM " + getDataTableName() + " LEFT JOIN " + getPlayersTableName()
                + " ON " + getDataTableName() + ".UUID=" + getPlayersTableName() + ".UUID WHERE " + getWhereConditionForUUID(uuids.size());
        try (PreparedStatement statement = this.database.getConnection().prepareStatement(sql)) {
            Map<UUID, EnderChestSnapshot> resultMap = new HashMap<>();
            Iterator<UUID> uuidIterator = uuids.iterator();
            int i = 0;
            while (i < uuids.size()) {
                statement.setString(i + 1, uuidIterator.next().toString());
                i++;
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ItemStack[] items = ItemStackSerializer.deserialize(resultSet.getString("CONTENTS"));
                int rows = resultSet.getInt("ROWS");
                UUID uuid = UUID.fromString(resultSet.getString("UUID"));
                String name = resultSet.getString("NAME");
                EnderChestSnapshot snapshot = new EnderChestSnapshot(uuid, name, items, rows);
                resultMap.put(uuid, snapshot);
            }

            return resultMap;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getWhereConditionForUUID(int num) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            stringBuilder.append(getDataTableName()).append(".UUID=?");
            if (i == num - 1)
                stringBuilder.append(";");
            else
                stringBuilder.append("OR ");
        }
        return stringBuilder.toString();
    }

    @Override
    public Map<String, EnderChestSnapshot> loadEnderChestsByName(Set<String> names) {
        if (names.isEmpty())
            return new HashMap<>();
        String sql = "SELECT " + getDataTableName() + ".UUID," + getPlayersTableName() + ".NAME,ROWS,CONTENTS FROM " + getDataTableName() + " LEFT JOIN " + getPlayersTableName()
                + " ON " + getDataTableName() + ".UUID=" + getPlayersTableName() + ".UUID WHERE " + getWhereConditionForNames(names.size());
        try (PreparedStatement statement = this.database.getConnection().prepareStatement(sql)) {
            Map<String, EnderChestSnapshot> resultMap = new HashMap<>();
            Iterator<String> nameIterator = names.iterator();
            int i = 0;
            while (i < names.size()) {
                statement.setString(i + 1, nameIterator.next());
                i++;
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ItemStack[] items = ItemStackSerializer.deserialize(resultSet.getString("CONTENTS"));
                int rows = resultSet.getInt("ROWS");
                UUID uuid = UUID.fromString(resultSet.getString("UUID"));
                String name = resultSet.getString("NAME");
                EnderChestSnapshot snapshot = new EnderChestSnapshot(uuid, name, items, rows);
                resultMap.put(name.toLowerCase(), snapshot);
            }

            return resultMap;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getWhereConditionForNames(int num) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            stringBuilder.append(dataTableName).append(".UUID = (SELECT ").append("UUID FROM ")
                    .append(playersTableName).append(" WHERE ").append("NAME=? LIMIT 1)");
            if (i == num - 1)
                stringBuilder.append(";");
            else
                stringBuilder.append("OR ");
        }
        return stringBuilder.toString();
    }
}
