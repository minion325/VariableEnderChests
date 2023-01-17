package me.saif.betterenderchests.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandImpl extends Command {

    private final PluginCommand command;

    protected CommandImpl(PluginCommand command) {
        super(command.getName());
        this.setAliases(command.getAliases());

        this.command = command;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        this.command.onCommand(sender, commandLabel, args);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return this.command.onTabComplete(sender, alias, args);
    }
}
