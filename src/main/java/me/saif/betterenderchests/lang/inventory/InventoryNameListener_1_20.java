package me.saif.betterenderchests.lang.inventory;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.enderchest.EnderChest;
import me.saif.betterenderchests.lang.locale.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class InventoryNameListener_1_20 implements Listener {

    private final VariableEnderChests plugin;
    private Method text, titleOverride;

    public InventoryNameListener_1_20(VariableEnderChests plugin) {
        this.plugin = plugin;

        if (VariableEnderChests.isPaper()) {
            try {
                Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
                this.text = componentClass.getMethod("text", String.class);
                this.titleOverride = InventoryOpenEvent.class.getMethod("titleOverride", componentClass);
            } catch (Exception ignored) {
            }
        }
    }

    @EventHandler
    private void onInvOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() == null)
            return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof EnderChest) && !(holder instanceof EnderChest.EnderChestRetreiver))
            return;

        String originalTitle = event.getView().getOriginalTitle();

        Map.Entry<String, Integer> ownerSizePair = InvMultilangCommons.parseInventoryName(originalTitle);

        if (ownerSizePair == null)
            return;

        Locale loc = plugin.getPlayerLocaleFinder().getLocale(((Player) event.getPlayer()));

        String newName;

        if (ownerSizePair.getValue() == -1) {
            newName = loc.getSingleFormattedMessage(InvMultilangCommons.RETRIEVAL_NAME, InvMultilangCommons.PLAYER_NAME_PLACEHOLDER.getResult(ownerSizePair.getKey()));
        } else {
            newName = loc.getSingleFormattedMessage(InvMultilangCommons.SIZE_NAME_MAP.get(ownerSizePair.getValue()), InvMultilangCommons.PLAYER_NAME_PLACEHOLDER.getResult(ownerSizePair.getKey()));
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> event.getView().setTitle(newName));

        //This is a 1.20+ paper feature to set the inv name via the event
        //we can then update the actual inv name a tick later
        if (VariableEnderChests.isPaper()) {
            if (this.text == null || this.titleOverride == null)
                return;

            try {
                this.titleOverride.invoke(event, this.text.invoke(null, newName));
            } catch (InvocationTargetException | IllegalAccessException ignored) {
            }
        }

    }


}
