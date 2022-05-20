package me.saif.betterenderchests.data;

import me.saif.betterenderchests.BetterEnderChests;
import me.saif.betterenderchests.enderchest.EnderChestSnapshot;
import me.saif.betterenderchests.utils.Manager;

import java.util.*;

public abstract class DataManager extends Manager<BetterEnderChests> {

    public DataManager(BetterEnderChests plugin) {
        super(plugin);
    }

    public abstract void init();

    public abstract void finishUp();

    public void saveEnderChest(UUID uuid, EnderChestSnapshot snapshot) {
        Map<UUID, EnderChestSnapshot> map = new HashMap<>();
        map.put(uuid, snapshot);
        saveEnderChestMultiple(map);
    }

    public abstract void saveEnderChestMultiple(Map<UUID, EnderChestSnapshot> snapshotMap);

    public abstract void saveNameAndUUID(String name, UUID uuid);

    public EnderChestSnapshot loadEnderChest(UUID uuid) {
        Map<UUID, EnderChestSnapshot> enderChestSnapshotMap = loadEnderChestsByUUID(Collections.singleton(uuid));
        return enderChestSnapshotMap.get(uuid);
    }

    public abstract Map<UUID, EnderChestSnapshot> loadEnderChestsByUUID(Set<UUID> uuids);

    public EnderChestSnapshot loadEnderChest(String name) {
        Map<String, EnderChestSnapshot> enderChestSnapshotMap = loadEnderChestsByName(Collections.singleton(name));
        return enderChestSnapshotMap.get(name);
    }

    public abstract Map<String, EnderChestSnapshot> loadEnderChestsByName(Set<String> names);

}
