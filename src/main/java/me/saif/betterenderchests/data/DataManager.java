package me.saif.betterenderchests.data;

import me.saif.betterenderchests.enderchest.EnderChestSnapshot;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public interface DataManager {

    void init();

    void finishUp();

    default void saveEnderChest(UUID uuid, EnderChestSnapshot snapshot) {
        Map<UUID, EnderChestSnapshot> map = new HashMap<>();
        map.put(uuid, snapshot);
        saveEnderChestMultiple(map);
    }

    void saveEnderChestMultiple(Map<UUID, EnderChestSnapshot> snapshotMap);

    void saveNameAndUUIDs(Map<String, UUID> map);

    void saveNameAndUUID(String name, UUID uuid);

    default EnderChestSnapshot loadEnderChest(UUID uuid) {
        Map<UUID, EnderChestSnapshot> enderChestSnapshotMap = loadEnderChestsByUUID(Collections.singleton(uuid));
        return enderChestSnapshotMap.get(uuid);
    }

    Map<UUID, EnderChestSnapshot> loadEnderChestsByUUID(Set<UUID> uuids);

    default EnderChestSnapshot loadEnderChest(String name) {
        name = name.toLowerCase();
        Map<String, EnderChestSnapshot> enderChestSnapshotMap = loadEnderChestsByName(Collections.singleton(name));
        return enderChestSnapshotMap.get(name);
    }

    Map<String, EnderChestSnapshot> loadEnderChestsByName(Set<String> names);

    Set<UUID> getAllEnderChests();

    void deleteEnderChest(UUID uuid);

    void deleteEnderChest(String name);

    boolean createBackup(Logger logger, File backupsFolder);

    void purge(char... confirm);

}
