package me.saif.betterenderchests.hooks;

import de.jeff_media.chestsort.api.ChestSortAPI;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class ChestSortHook {

    private static boolean hooked = false;

    public static boolean hook() {
        if (Bukkit.getPluginManager().getPlugin("ChestSort") == null)
            return hooked = false;
        return hooked = true;
    }

    public static boolean isHooked() {
        return hooked;
    }

    public static void setSortable(Inventory inventory) {
        if (isHooked())
            ChestSortAPI.setSortable(inventory);
    }

    public static void setUnsortable(Inventory inventory) {
        if (isHooked())
            ChestSortAPI.setUnsortable(inventory);
    }

}
