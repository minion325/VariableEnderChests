package me.saif.betterenderchests.lang.locale;

import me.saif.betterenderchests.VariableEnderChests;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerLocaleLoader implements Listener {

    private VariableEnderChests plugin;
    private Map<UUID, String> playerLocaleMap = new HashMap<>();

    public PlayerLocaleLoader(VariableEnderChests plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    //TODO implement logic
    public String getLocale(Player player) {
        return "en_US";
    }

}
