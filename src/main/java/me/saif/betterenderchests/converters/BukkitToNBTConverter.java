package me.saif.betterenderchests.converters;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.enderchest.EnderChestSnapshot;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BukkitToNBTConverter extends Converter {

    private static final int BATCH_AMOUNT = 100;

    public BukkitToNBTConverter(VariableEnderChests plugin) {
        super(plugin, "BukkitToNBT");
    }

    @Override
    public boolean convert(String... args) {
        if (Bukkit.getOnlinePlayers().size() > 0)
            throw new IllegalStateException("Cannot convert with players online");

        this.plugin.getDataManager().createBackup(plugin.getLogger(), new File(plugin.getDataFolder(), "backups"));

        Set<UUID> allOld = plugin.getDataManager().getAllEnderChests();

        Set<UUID> uuids = new HashSet<>();

        for (UUID uuid : allOld) {
            uuids.add(uuid);

            if (uuids.size() != BATCH_AMOUNT)
                continue;

            migrate(uuids, this.plugin.getDataManager());
            uuids.clear();
        }

        if (uuids.size() != 0) {
            migrate(uuids, this.plugin.getDataManager());
            uuids.clear();
        }

        this.plugin.getLogger().info("Migration Complete!");
        this.plugin.getLogger().info("Please restart your server!");

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> Bukkit.getPluginManager().disablePlugin(this.plugin));
        return true;
    }

    private void migrate(Set<UUID> uuids, DataManager oldDataManager) {
        Map<UUID, EnderChestSnapshot> snapshotMap = oldDataManager.loadEnderChestsByUUID(uuids);

        oldDataManager.saveEnderChestMultiple(snapshotMap);

        this.plugin.getLogger().info("Migrated data for " + uuids);
    }
}
