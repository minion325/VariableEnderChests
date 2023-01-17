package me.saif.betterenderchests.command;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class PluginCommand {

    private final String name;
    private final List<String> aliases;

    public PluginCommand(String name, String... aliases) {
        this.name = name;
        this.aliases = Arrays.asList(aliases);
    }

    public abstract void onCommand(CommandSender sender, String alias, String[] args);

    public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return Collections.unmodifiableList(aliases);
    }
}
