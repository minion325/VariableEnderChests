package me.saif.betterenderchests.utils;

import org.bukkit.plugin.java.JavaPlugin;

public class Manager<T extends JavaPlugin> {

    private final T plugin;

    public Manager(T plugin) {
        this.plugin = plugin;
    }

    public T getPlugin() {
        return plugin;
    }
}
