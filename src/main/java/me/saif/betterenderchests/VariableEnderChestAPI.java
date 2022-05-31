package me.saif.betterenderchests;

import me.saif.betterenderchests.enderchest.EnderChest;
import me.saif.betterenderchests.utils.Callback;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VariableEnderChestAPI {

    private VariableEnderChests plugin;

    public VariableEnderChestAPI(VariableEnderChests plugin) {
        this.plugin = plugin;
    }

    public Callback<EnderChest> getEnderChest(String name) {
        return plugin.getEnderChestManager().getEnderChest(name);
    }

    public Callback<EnderChest> getEnderChest(UUID uuid) {
        return plugin.getEnderChestManager().getEnderChest(uuid);
    }

    public EnderChest getEnderChest(Player player) {
        return plugin.getEnderChestManager().getEnderChest(player);
    }

    public void openEnderChest(EnderChest enderChest, Player player) {
        plugin.getEnderChestManager().openEnderChest(enderChest, player);
    }

    public void openEnderChest(Player player, int rows) {
        if (rows == 0)
            return;
        this.plugin.getEnderChestManager().openEnderChest(player, rows);
    }

    public void openEnderChest(Player player) {
        int rows = this.plugin.getEnderChestManager().getNumRows(player);
        this.openEnderChest(player, rows);
    }

    public void openEnderChest(EnderChest chest, Player player, int rows) {
        if (rows == 0)
            return;
        plugin.getEnderChestManager().openEnderChest(chest, player, rows);
    }

    public int getRows(Player player) {
        return this.plugin.getEnderChestManager().getNumRows(player);
    }

}
