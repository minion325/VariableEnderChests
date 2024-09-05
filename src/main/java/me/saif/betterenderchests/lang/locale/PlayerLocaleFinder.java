package me.saif.betterenderchests.lang.locale;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import me.saif.betterenderchests.VariableEnderChests;
import me.saif.reflectionutils.ReflectionUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlayerLocaleFinder {

    private VariableEnderChests plugin;
    private Method getLocaleMethod;


    public PlayerLocaleFinder(VariableEnderChests plugin) {
        this.plugin = plugin;

        if (!MinecraftVersion.isNewerThan(MinecraftVersion.MC1_11_R1)) {
            getLocaleMethod = ReflectionUtils.getMethod(Player.Spigot.class, "getLocale", true).get();
        }
    }

    public Locale getLocale(Player player) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_12_R1))
            return plugin.getLocaleLoader().getOrDefault(player.getLocale());
        try {
            return plugin.getLocaleLoader().getOrDefault((String) getLocaleMethod.invoke(player.spigot()));
        } catch (IllegalAccessException | InvocationTargetException e) {
            return plugin.getLocaleLoader().getDefaultLocale();
        }
    }

}
