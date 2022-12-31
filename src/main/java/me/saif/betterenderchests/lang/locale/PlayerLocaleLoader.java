package me.saif.betterenderchests.lang.locale;

import me.saif.betterenderchests.VariableEnderChests;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLocaleLoader implements Listener {

    private VariableEnderChests plugin;
    private Map<UUID, String> playerLocaleMap = new ConcurrentHashMap<>();

    public PlayerLocaleLoader(VariableEnderChests plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    //TODO implement logic
    public Locale getLocale(Player player) {
        return plugin.getLocaleManager().getOrDefault(player.getLocale());
    }

}
