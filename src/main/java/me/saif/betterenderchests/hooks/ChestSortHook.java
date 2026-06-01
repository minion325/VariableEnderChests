package me.saif.betterenderchests.hooks;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Method;

public class ChestSortHook {

    private static boolean hooked = false;
    private static Method setSortableMethod;
    private static Method setUnsortableMethod;

    public static boolean hook() {
        if (Bukkit.getPluginManager().getPlugin("ChestSort") == null)
            return hooked = false;
        try {
            Class<?> api = Class.forName("de.jeff_media.chestsort.api.ChestSortAPI");
            setSortableMethod = api.getMethod("setSortable", Inventory.class);
            setUnsortableMethod = api.getMethod("setUnsortable", Inventory.class);
            return hooked = true;
        } catch (Throwable t) {
            return hooked = false;
        }
    }

    public static boolean isHooked() {
        return hooked;
    }

    public static void setSortable(Inventory inventory) {
        if (!isHooked() || setSortableMethod == null) return;
        try {
            setSortableMethod.invoke(null, inventory);
        } catch (Throwable ignored) {
        }
    }

    public static void setUnsortable(Inventory inventory) {
        if (!isHooked() || setUnsortableMethod == null) return;
        try {
            setUnsortableMethod.invoke(null, inventory);
        } catch (Throwable ignored) {
        }
    }

}
