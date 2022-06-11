package me.saif.betterenderchests.enderchest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;

public class EnderChestClickListener implements Listener {

    private static final String PERMISSION_TO_EDIT = "enderchest.modify.others";

    @EventHandler
    private void onInventoryInteract(InventoryClickEvent event) {
        onInvInteract(event);
    }

    @EventHandler
    private void onInventoryInteract(InventoryDragEvent event) {
        onInvInteract(event);
    }

    private void onInvInteract(InventoryInteractEvent event) {
        Inventory inventory = event.getWhoClicked().getOpenInventory().getTopInventory();

        if (inventory == null || inventory.getHolder() == null || !(inventory.getHolder() instanceof EnderChest))
            return;

        if (!event.getWhoClicked().hasPermission(PERMISSION_TO_EDIT) && !event.getWhoClicked().getUniqueId().equals(((EnderChest) inventory.getHolder()).getUUID()))
            event.setCancelled(true);
    }

}
