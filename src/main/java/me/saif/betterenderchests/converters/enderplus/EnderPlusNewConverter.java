package me.saif.betterenderchests.converters.enderplus;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.converters.Converter;
import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.enderchest.EnderChestSnapshot;
import me.saif.betterenderchests.utils.ItemStackSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class EnderPlusNewConverter extends Converter {

    private static final int BATCH_AMOUNT = 100;

    public EnderPlusNewConverter(VariableEnderChests plugin) {
        super(plugin, "EnderPlusNew");
    }

    @Override
    public boolean convert(String... args) {
        if (Bukkit.getOnlinePlayers().size() > 0)
            throw new IllegalStateException("Cannot convert with players online");

        Plugin enderPlusPlugin = Bukkit.getPluginManager().getPlugin("EnderPlus");

        if (enderPlusPlugin == null)
            throw new IllegalStateException("Cannot convert using " + this.getName() + " while EnderPlus isn't running.");

        boolean online = enderPlusPlugin.getConfig().getBoolean("Config.Online");

        this.plugin.getDataManager().createBackup();
        this.plugin.getEnderChestManager().finishUp();

        boolean overwrite = args.length > 0 && args[0] != null && args[0].equalsIgnoreCase("overwrite");

        List<OfflinePlayer> offlinePlayers = new ArrayList<>(Arrays.asList(Bukkit.getOfflinePlayers()));

        if (!overwrite) {
            Set<UUID> VECEnderChests = this.plugin.getDataManager().getAllEnderChests();

            offlinePlayers.removeIf(offlinePlayer -> VECEnderChests.contains(offlinePlayer.getUniqueId()));
        }

        Map<UUID, EnderChestSnapshot> snapshotMap = new HashMap<>();
        for (OfflinePlayer player : offlinePlayers) {
            EnderChestSnapshot snapshot = loadEnderChest(enderPlusPlugin, player, online);

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

    private EnderChestSnapshot loadEnderChest(Plugin enderChestPlugin, OfflinePlayer offlinePlayer, boolean online) {
        try {
            String encodedData;

            Object db = enderChestPlugin.getClass().getMethod("getDatabase").invoke(null);

            if (online) {
                encodedData = (String) db.getClass().getMethod("getDataByUuid", String.class).invoke(db, offlinePlayer.getUniqueId().toString());
            } else {
                encodedData = (String) db.getClass().getMethod("getDataByPlayerName", String.class).invoke(db, offlinePlayer.getName());
            }

            //my deserializer might be able to deserialize it?
            if (encodedData != null) {
                ItemStack[] deserializedItems = ItemStackSerializer.deserialize(encodedData);
                return new EnderChestSnapshot(offlinePlayer.getUniqueId(), offlinePlayer.getName(), deserializedItems, 6);
            } else
                return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
