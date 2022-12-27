package me.saif.betterenderchests.commands;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.converters.Converter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ConversionCommand {

    private VariableEnderChests plugin;
    private Map<CommandSender, Converter> confirmMap = new HashMap<>();

    public ConversionCommand(VariableEnderChests plugin) {
        this.plugin = plugin;
    }

    @Command({"echestconvert", "enderchestconvert"})
    @AutoComplete("@converters")
    public void onConvertCommand(ConsoleCommandSender sender, @Named("converter") String converterName) {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            sender.sendMessage("All players must be offline to use this command");
            return;
        }
        Converter converter = this.plugin.getConverterManager().getConverter(converterName);
        if (converter == null) {
            sender.sendMessage("That is not a valid converter.");
            sender.sendMessage("Valid converters");
            this.plugin.getConverterManager().getConverters().stream().map(Converter::getName).forEach(sender::sendMessage);
            return;
        }

        if (this.confirmMap.get(sender) != converter) {
            this.confirmMap.put(sender, converter);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> confirmMap.remove(sender), 100L);
            sender.sendMessage("You need to execute this command again to be sure that you would like to convert.");
            sender.sendMessage("This may result in everyone online being kicked from the server or major lag.");
            sender.sendMessage("This will also result in the loss of all data being stored by this plugin");
            sender.sendMessage("Proceed with caution!");
            return;
        }
        this.confirmMap.remove(sender);
        sender.sendMessage("Conversion from " + converter.getName() + " is starting.");
        boolean succuess = converter.convert();
        if (succuess)
            sender.sendMessage("Conversion successfully finished");
        else
            sender.sendMessage("Conversion failed");
    }

}
