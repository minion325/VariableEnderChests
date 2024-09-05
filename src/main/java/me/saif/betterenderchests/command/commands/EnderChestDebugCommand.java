package me.saif.betterenderchests.command.commands;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.command.PluginCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderChestDebugCommand extends PluginCommand {

    private VariableEnderChests plugin;

    public EnderChestDebugCommand(VariableEnderChests plugin) {
        super("vecdebug");
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender sender, String alias, String[] args) {
        if (args.length == 0) {
            cmdDefault(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("updaterows")) {
            updateRows();
            sender.sendMessage("Updated enderchest rows for all players.");
            return;
        }

        cmdDefault(sender);
    }

    private void updateRows() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            this.plugin.getEnderChestManager().updateRows(onlinePlayer);
        }
    }

    private void cmdDefault(CommandSender sender) {
        sender.sendMessage("You are running " + this.plugin.getName() + " " + this.plugin.getDescription().getVersion());
    }

    @Override
    public String getPermission() {
        return "enderchest.debug";
    }
}
