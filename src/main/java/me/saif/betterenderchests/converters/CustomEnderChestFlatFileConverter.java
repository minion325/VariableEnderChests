package me.saif.betterenderchests.converters;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.converters.enderplus.EnderPlusConverter;
import me.saif.betterenderchests.enderchest.EnderChestSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CustomEnderChestFlatFileConverter extends Converter{

    public CustomEnderChestFlatFileConverter(VariableEnderChests plugin) {
        super(plugin, "CustomEnderChestFlatFile");
    }

    @Override
    public boolean convert() {
        if (Bukkit.getOnlinePlayers().size() > 0)
            throw new IllegalStateException("Cannot convert with players online");

        this.plugin.getEnderChestManager().finishUp();
        this.plugin.getDataManager().createBackup();
        this.plugin.getDataManager().purge('Y', 'E', 'S');

        Map<UUID, EnderChestSnapshot> chests = new HashMap<>();
        Map<String, UUID> nameUUIDMap = new HashMap<>();
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            ItemStack[] contents = loadEnderChest(offlinePlayer.getUniqueId());

            if (contents == null)
                continue;

            nameUUIDMap.put(offlinePlayer.getName(), offlinePlayer.getUniqueId());
            chests.put(offlinePlayer.getUniqueId(), new EnderChestSnapshot(offlinePlayer.getUniqueId(), offlinePlayer.getName(), contents, 6));
        }

        this.plugin.getDataManager().saveNameAndUUIDs(nameUUIDMap);
        this.plugin.getDataManager().saveEnderChestMultiple(chests);

        return true;
    }

    public ItemStack[] loadEnderChest(UUID playerUUID) {
        File dataFile = new File("plugins" + System.getProperty("file.separator") + "CustomEnderChest" + System.getProperty("file.separator") + "PlayerData", playerUUID + ".yml");

        if (!dataFile.exists())
            return null;

        FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
        ItemStack[] itemsList = new ItemStack[54];
        for (int i = 0; i < itemsList.length; i++) {
            itemsList[i] = ymlFormat.getItemStack("EnderChestInventory." + i);
        }

        return itemsList;
    }
}
