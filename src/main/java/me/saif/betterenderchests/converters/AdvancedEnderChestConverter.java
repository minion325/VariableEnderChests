package me.saif.betterenderchests.converters;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.enderchest.EnderChestSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.*;

public class AdvancedEnderChestConverter extends Converter {

    private static final int BATCH_AMOUNT = 100;

    public AdvancedEnderChestConverter(VariableEnderChests plugin) {
        super(plugin, "AdvancedEnderchest");
    }

    @Override
    public boolean convert(String... args) {
        if (Bukkit.getOnlinePlayers().size() > 0)
            throw new IllegalStateException("Cannot convert with players online");

        Plugin aecPlugin = Bukkit.getPluginManager().getPlugin("AdvancedEnderchest");

        if (aecPlugin == null)
            throw new IllegalStateException("Cannot convert using " + this.getName() + " while AdvancedEnderchest isn't running.");

        boolean single = !aecPlugin.getConfig().getBoolean("settings.enable-multi-mode");

        if (!single)
            throw new IllegalStateException("Cannot convert using " + this.getName() + " while AdvancedEnderchest is using multi-mode.");

        this.plugin.getDataManager().createBackup(plugin.getLogger(), new File(plugin.getDataFolder(), "backups"));
        this.plugin.getEnderChestManager().finishUp();

        boolean overwrite = args.length > 0 && args[0] != null && args[0].equalsIgnoreCase("overwrite");

        List<OfflinePlayer> offlinePlayers = new ArrayList<>(Arrays.asList(Bukkit.getOfflinePlayers()));

        if (!overwrite) {
            Set<UUID> VECEnderChests = this.plugin.getDataManager().getAllEnderChests();

            offlinePlayers.removeIf(offlinePlayer -> VECEnderChests.contains(offlinePlayer.getUniqueId()));
        }

        Map<UUID, EnderChestSnapshot> snapshotMap = new HashMap<>();
        for (OfflinePlayer player : offlinePlayers) {
            EnderChestSnapshot snapshot = loadEnderChest(player);

            if(snapshot == null)
                continue;

            snapshotMap.put(player.getUniqueId(), snapshot);

            if (snapshotMap.size() < BATCH_AMOUNT)
                continue;

            migrate(snapshotMap, this.plugin.getDataManager());
            snapshotMap.clear();
        }

        if (snapshotMap.size() != 0) {
            migrate(snapshotMap, this.plugin.getDataManager());
            snapshotMap.clear();
        }

        this.plugin.getLogger().info("Migration Complete!");
        this.plugin.getLogger().info("Please restart your server!");

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> Bukkit.getPluginManager().disablePlugin(this.plugin));
        return true;
    }

    private void migrate(Map<UUID, EnderChestSnapshot> snapshotMap, DataManager dataManager) {
        dataManager.saveEnderChestMultiple(snapshotMap);

        this.plugin.getLogger().info("Migrated data for " + snapshotMap.keySet());
    }

    private EnderChestSnapshot loadEnderChest(OfflinePlayer offlinePlayer) {
        try {
            Method getResultSync = Class.forName("de.chriis.advancedenderchest.database.Database").getMethod("getResultSync", String.class);
            ResultSet results = (ResultSet) getResultSync.invoke(null, String.format("SELECT * FROM %s WHERE uuid='%s' AND chest_id='aec.single'", Class.forName("de.chriis.advancedenderchest.database.DatabaseTables").getField("CHESTS").get(null), offlinePlayer.getUniqueId()));

            if (!results.next())
                return null;

            Object isb64 = Class.forName("de.chriis.advancedenderchest.manager.ItemStackBase64").getConstructor(String.class).newInstance(results.getString("items"));
            ItemStack[] array = (ItemStack[]) Class.forName("de.chriis.advancedenderchest.manager.ItemStackBase64").getMethod("toItemStacks").invoke(isb64);
            //my deserializer might be able to deserialize it?
            if (array != null) {
                return new EnderChestSnapshot(offlinePlayer.getUniqueId(), offlinePlayer.getName(), array, 6);
            } else {
                throw new RuntimeException(String.format("Error loading data for %s - Are they migrated?", offlinePlayer.getUniqueId()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
