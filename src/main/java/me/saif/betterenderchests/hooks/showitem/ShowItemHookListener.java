package me.saif.betterenderchests.hooks.showitem;

import de.themoep.ShowItem.api.data.EnderData;
import de.themoep.ShowItem.api.data.LiveEnderData;
import de.themoep.ShowItem.api.data.StaticEnderData;
import de.themoep.ShowItem.api.event.ViewEnderchestEvent;
import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.enderchest.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class ShowItemHookListener implements Listener {

    private VariableEnderChests plugin;

    public ShowItemHookListener(VariableEnderChests plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onEChestShow(ViewEnderchestEvent event) {
        EnderData enderData = event.getShownData();
        EnderChest chest = plugin.getEnderChestManager().getEnderChest(event.getPlayer());
        if (enderData instanceof StaticEnderData) {
            event.setShownData(new CustomStaticEnderData(event.getPlayer(), chest));
        } else if (enderData instanceof LiveEnderData) {
            event.setShownData(new CustomLiveEnderData(event.getPlayer(), chest));
        }
    }

    private static class CustomLiveEnderData extends LiveEnderData {

        private final EnderChest enderChest;

        public CustomLiveEnderData(Player player, EnderChest enderChest) {
            super(player);
            this.enderChest = enderChest;
        }

        @Override
        public ItemStack[] getStorageContents() {
            if (this.isValid()) {
                return enderChest.getInventory().getContents();
            } else
                return new ItemStack[InventoryType.ENDER_CHEST.getDefaultSize()];
        }
    }

    private static class CustomStaticEnderData extends StaticEnderData {

        private final ItemStack[] contentsArray;

        public CustomStaticEnderData(Player player, EnderChest enderChest) {
            super(player);
            contentsArray = enderChest.getInventory().getContents();
        }

        @Override
        public ItemStack[] getStorageContents() {
            return this.contentsArray;
        }
    }

}
