package me.saif.betterenderchests.commands;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.converters.Converter;
import me.saif.betterenderchests.lang.MessageKey;
import me.saif.betterenderchests.lang.Messenger;
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
    private Messenger messenger;

    public ConversionCommand(VariableEnderChests plugin) {
        this.plugin = plugin;
        this.messenger = plugin.getMessenger();
    }

    @Command({"echestconvert", "enderchestconvert"})
    @AutoComplete("@converters")
    public void onConvertCommand(ConsoleCommandSender sender, @Named("converter") String converterName) {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            messenger.sendMessage(sender, MessageKey.ALL_PLAYERS_OFFLINE);
            return;
        }
        Converter converter = this.plugin.getConverterManager().getConverter(converterName);
        if (converter == null) {
            messenger.sendMessage(sender, MessageKey.NOT_VALID_CONVERTER);
            messenger.sendMessage(sender, MessageKey.LIST_VALID_CONVERTERS);
            this.plugin.getConverterManager().getConverters().stream().map(Converter::getName).forEach(sender::sendMessage);
            return;
        }

        if (this.confirmMap.get(sender) != converter) {
            this.confirmMap.put(sender, converter);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> confirmMap.remove(sender), 100L);
            messenger.sendMessage(sender, MessageKey.CONFIRM_CONVERTER);
            return;
        }
        this.confirmMap.remove(sender);
        messenger.sendMessage(sender, MessageKey.CONVERSION_STARTING);
        boolean succuess = converter.convert();
        if (succuess)
            messenger.sendMessage(sender, MessageKey.CONVERSION_SUCCESS);
        else
            messenger.sendMessage(sender, MessageKey.CONVERSION_FAILURE);
    }

}
