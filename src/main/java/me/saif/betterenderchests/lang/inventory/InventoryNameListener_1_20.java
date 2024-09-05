package me.saif.betterenderchests.lang.inventory;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.enderchest.EnderChest;
import me.saif.betterenderchests.lang.locale.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Map;

public class InventoryNameListener_1_20 implements Listener {

    private final VariableEnderChests plugin;

    public InventoryNameListener_1_20(VariableEnderChests plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onInvOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() == null || !(event.getInventory().getHolder() instanceof EnderChest))
            return;

        String originalTitle = event.getView().getOriginalTitle();

        Map.Entry<String, Integer> ownerSizePair = InvMultilangCommons.parseInventoryName(originalTitle);

        if (ownerSizePair == null)
            return;

        Locale loc = plugin.getPlayerLocaleFinder().getLocale(((Player) event.getPlayer()));

        String newName = loc.getSingleFormattedMessage(InvMultilangCommons.SIZE_NAME_MAP.get(ownerSizePair.getValue()), InvMultilangCommons.PLAYER_NAME_PLACEHOLDER.getResult(ownerSizePair.getKey()));

        event.getView().setTitle(newName);
        //Bukkit.getScheduler().runTask(this.plugin, () -> event.getView().setTitle(newName));

    }


}
