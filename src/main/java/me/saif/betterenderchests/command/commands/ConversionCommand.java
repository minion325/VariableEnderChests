package me.saif.betterenderchests.command.commands;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.command.PluginCommand;
import me.saif.betterenderchests.converters.Converter;
import me.saif.betterenderchests.lang.MessageKey;
import me.saif.betterenderchests.lang.Messenger;
import me.saif.betterenderchests.lang.placeholder.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConversionCommand extends PluginCommand {

    private final VariableEnderChests plugin;
    private final Map<CommandSender, Converter> confirmMap = new HashMap<>();
    private final Placeholder<String> usagePlaceholder = Placeholder.getStringPlaceholder("command");
    private final Placeholder<Converter> converterPlaceholder = Placeholder.getPlaceholder("converter", Converter::getName);
    private final Messenger messenger;

    public ConversionCommand(VariableEnderChests plugin) {
        super("enderchestconvert", "echestconvert");
        this.plugin = plugin;
        this.messenger = plugin.getMessenger();
    }

    @Override
    public void onCommand(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            messenger.sendMessage(sender, MessageKey.COMMAND_CONSOLE_ONLY);
            return;
        }

        if (args.length == 0) {
            messenger.sendMessage(sender, MessageKey.COMMAND_USAGE, usagePlaceholder.getResult(alias + " <converter>"));
            return;
        }

        String[] flags = args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);
        onConvertCommand((ConsoleCommandSender) sender, args[0]);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) || args.length != 1)
            return null;
        else return plugin.getConverterManager().getConverters().stream()
                .map(Converter::getName)
                .filter(converter -> converter.toLowerCase(Locale.ENGLISH).startsWith(args[0].toLowerCase(Locale.ENGLISH)))
                .collect(Collectors.toList());
    }

    public void onConvertCommand(ConsoleCommandSender sender, String converterName, String... flags) {
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
        messenger.sendMessage(sender, MessageKey.CONVERSION_STARTING, converterPlaceholder.getResult(converter));

        this.plugin.getConverterManager().setConverting(true);
        CompletableFuture<Boolean> success = CompletableFuture.supplyAsync(() ->
                converter.convert(flags)
        ).exceptionally(throwable -> {
            throwable.printStackTrace();
            return false;
        });

        success.thenAccept(aBoolean -> {
            if (aBoolean)
                messenger.sendMessage(sender, MessageKey.CONVERSION_SUCCESS);
            else
                messenger.sendMessage(sender, MessageKey.CONVERSION_FAILURE);

            this.plugin.getConverterManager().setConverting(false);
        });
    }
}
