package me.saif.betterenderchests.enderchest;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.lang.MessageKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnderChestClickListener implements Listener {

    private static final String PERMISSION_TO_EDIT = "enderchest.modify.others";

    private VariableEnderChests plugin;

    public EnderChestClickListener(VariableEnderChests plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onInventoryInteract(InventoryClickEvent event) {
        if (!isEnderChest(event.getWhoClicked().getOpenInventory().getTopInventory()))
            return;

        onInvInteract(event);

        if (event.isCancelled())
            return;

        if (event.getCursor() != null && event.getRawSlot() < event.getWhoClicked().getOpenInventory().getTopInventory().getSize() && plugin.getEnderChestManager().getBlacklist().contains(event.getCursor().getType())) {
            event.setCancelled(true);
            plugin.getMessenger().sendMessage(event.getWhoClicked(), MessageKey.BLACKLIST_MESSAGE);
        }
        else if (event.getClick().isShiftClick() && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getRawSlot() >= event.getWhoClicked().getOpenInventory().getTopInventory().getSize()) {
            if (event.getCurrentItem() != null && plugin.getEnderChestManager().getBlacklist().contains(event.getCurrentItem().getType())) {
                event.setCancelled(true);
                plugin.getMessenger().sendMessage(event.getWhoClicked(), MessageKey.BLACKLIST_MESSAGE);
            }
        } else if (event.getClick() == ClickType.NUMBER_KEY && event.getRawSlot() < event.getWhoClicked().getOpenInventory().getTopInventory().getSize()) {
            ItemStack hotbarItem = event.getWhoClicked().getInventory().getContents()[event.getHotbarButton()];
            if (hotbarItem != null && plugin.getEnderChestManager().getBlacklist().contains(hotbarItem.getType())) {
                event.setCancelled(true);
                plugin.getMessenger().sendMessage(event.getWhoClicked(), MessageKey.BLACKLIST_MESSAGE);
            }
        } else if (VariableEnderChests.MC_VERSION >= 16) {
            ItemStack offHand = event.getWhoClicked().getInventory().getItemInOffHand();
            if (event.getClick() == ClickType.SWAP_OFFHAND && offHand != null && plugin.getEnderChestManager().getBlacklist().contains(offHand.getType())) {
                event.setCancelled(true);
                plugin.getMessenger().sendMessage(event.getWhoClicked(), MessageKey.BLACKLIST_MESSAGE);
            }
        }

    }

    @EventHandler
    private void onInventoryInteract(InventoryDragEvent event) {
        if (!isEnderChest(event.getWhoClicked().getOpenInventory().getTopInventory()))
            return;

        onInvInteract(event);

        if (event.isCancelled())
            return;

        if (event.getOldCursor() != null && event.getRawSlots().stream().anyMatch(integer -> integer < event.getWhoClicked().getOpenInventory().getTopInventory().getSize()) && plugin.getEnderChestManager().getBlacklist().contains(event.getOldCursor().getType())) {
            event.setCancelled(true);
            plugin.getMessenger().sendMessage(event.getWhoClicked(), MessageKey.BLACKLIST_MESSAGE);
        }

    }

    private void onInvInteract(InventoryInteractEvent event) {
        Inventory inventory = event.getWhoClicked().getOpenInventory().getTopInventory();

        if (!event.getWhoClicked().hasPermission(PERMISSION_TO_EDIT) && !event.getWhoClicked().getUniqueId().equals(((EnderChest) inventory.getHolder()).getUUID()))
            event.setCancelled(true);
    }

    private boolean isEnderChest(Inventory inventory) {
        return inventory != null && inventory.getHolder() != null && inventory.getHolder() instanceof EnderChest;
    }

}
