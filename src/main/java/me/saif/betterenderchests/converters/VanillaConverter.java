package me.saif.betterenderchests.converters;

import de.tr7zw.changeme.nbtapi.*;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.enderchest.EnderChestSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class VanillaConverter extends Converter {

    private static final int BATCH_AMOUNT = 100;
    private File playerdataFolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata");

    public VanillaConverter(VariableEnderChests plugin) {
        super(plugin, "VanillaConverter");
    }

    @Override
    public boolean convert(String... args) {
        if (Bukkit.getOnlinePlayers().size() > 0)
            throw new IllegalStateException("Cannot convert with players online");

        boolean overwrite = args.length > 0 && args[0] != null && args[0].equalsIgnoreCase("overwrite");

        this.plugin.getDataManager().createBackup();

        List<OfflinePlayer> offlinePlayers = new ArrayList<>(Arrays.asList(Bukkit.getOfflinePlayers()));

        if (!overwrite) {
            Set<UUID> VECEnderChests = this.plugin.getDataManager().getAllEnderChests();

            offlinePlayers.removeIf(offlinePlayer -> VECEnderChests.contains(offlinePlayer.getUniqueId()));
        }

        Map<UUID, EnderChestSnapshot> snapshotMap = new HashMap<>();
        for (OfflinePlayer player : offlinePlayers) {
            snapshotMap.put(player.getUniqueId(), loadEnderChest(player));

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
        File dataFile = new File(this.playerdataFolder, offlinePlayer.getUniqueId() + ".dat");

        try {
            NBTFile nbtFile = new NBTFile(dataFile);
            NBTCompoundList compoundList = nbtFile.getCompoundList("EnderItems");

            ItemStack[] contents = new ItemStack[54];

            for (int i = 0; i < compoundList.size(); i++) {
                NBTListCompound compound = compoundList.get(i);

                int slot = compound.getByte("Slot");
                contents[slot] = NBTItem.convertNBTtoItem(compound);
            }

            return new EnderChestSnapshot(offlinePlayer.getUniqueId(), offlinePlayer.getName(), contents, 6);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
