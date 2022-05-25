package me.saif.betterenderchests.converters;

import me.saif.betterenderchests.VariableEnderChests;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public abstract class Converter {

    private String name;
    protected VariableEnderChests plugin;

    public Converter(VariableEnderChests plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean convert();

}
