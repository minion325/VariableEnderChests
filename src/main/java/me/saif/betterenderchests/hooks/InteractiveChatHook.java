package me.saif.betterenderchests.hooks;

import com.loohp.interactivechat.api.events.InventoryPlaceholderEvent;
import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.enderchest.EnderChest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class InteractiveChatHook implements Listener {

    private VariableEnderChests plugin;

    public InteractiveChatHook(VariableEnderChests plugin) {
        if (Bukkit.getPluginManager().getPlugin("InteractiveChat") == null) {
            return;
        }
        plugin.getLogger().info("Hooked into InteractiveChat");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    private void onInventoryDisplay(InventoryPlaceholderEvent event) {
        if (event.getType() != InventoryPlaceholderEvent.InventoryPlaceholderType.ENDERCHEST)
            return;

        Player player = event.getSender().getLocalPlayer();
        plugin.getEnderChestManager().updateRows(player);
        EnderChest enderChest = plugin.getEnderChestManager().getEnderChest(player);

        event.setInventory(enderChest.getInventory());
    }

}
