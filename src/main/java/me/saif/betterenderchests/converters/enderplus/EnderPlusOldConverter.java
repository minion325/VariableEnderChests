package me.saif.betterenderchests.converters.enderplus;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.enderchest.EnderChestSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnderPlusOldConverter extends EnderPlusConverter {

    public EnderPlusOldConverter(VariableEnderChests plugin) {
        super(plugin, "EnderPlusOld");
    }

    @Override
    public boolean convert() {
        if (Bukkit.getOnlinePlayers().size() > 0)
            throw new IllegalStateException("Cannot convert with players online");

        try {
            FileConfiguration data = this.getConfig();
            if (data == null) {
                System.out.println("Nothing to convert");
                return false;
            }

            this.plugin.getEnderChestManager().finishUp();
            this.plugin.getDataManager().createBackup();
            this.plugin.getDataManager().purge('Y', 'E', 'S');

            Map<String, ItemStack[]> dataMap = new HashMap<>();
            data.getConfigurationSection("data").getKeys(false).forEach(key -> {
                List<ItemStack> contents = (List<ItemStack>) data.getList("data." + key);
                if (contents == null)
                    return;

                dataMap.put(key, contents.toArray(new ItemStack[0]));
            });

            Map<UUID, EnderChestSnapshot> chests = new HashMap<>();
            Map<String, UUID> nameUUIDMap = new HashMap<>();
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                ItemStack[] contents = dataMap.get(offlinePlayer.getName());
                if (contents == null)
                    continue;

                nameUUIDMap.put(offlinePlayer.getName(), offlinePlayer.getUniqueId());
                chests.put(offlinePlayer.getUniqueId(), new EnderChestSnapshot(offlinePlayer.getUniqueId(), offlinePlayer.getName(), contents, 6));
            }

            this.plugin.getDataManager().saveNameAndUUIDs(nameUUIDMap);
            this.plugin.getDataManager().saveEnderChestMultiple(chests);

            return true;
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            System.out.println("error loading data.yml for conversion");
            return false;
        }
    }
}
