package me.saif.betterenderchests;

import me.saif.betterenderchests.commands.ConversionCommand;
import me.saif.betterenderchests.commands.EnderChestCommand;
import me.saif.betterenderchests.converters.Converter;
import me.saif.betterenderchests.converters.ConverterManager;
import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.data.Messages;
import me.saif.betterenderchests.data.SQLiteDataManager;
import me.saif.betterenderchests.enderchest.EnderChestManager;
import me.saif.betterenderchests.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.core.BukkitHandler;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class VariableEnderChests extends JavaPlugin {

    private DataManager dataManager;
    private EnderChestManager enderChestManager;
    private Messages messages;
    private ConverterManager converterManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.messages = new Messages(this.getConfig());

        setupDataManager();
        setupEnderChestManager();
        setupCommands();
        setupMetricsAndCheckForUpdate();
    }

    private void setupDataManager() {
        this.dataManager = new SQLiteDataManager(this);
        this.dataManager.init();
    }

    private void setupEnderChestManager() {
        this.enderChestManager = new EnderChestManager(this);
        Bukkit.getPluginManager().registerEvents(this.enderChestManager, this);

        this.converterManager = new ConverterManager(this);
    }

    private void setupCommands() {
        CommandHandler commandHandler = new BukkitHandler(this)
                .getAutoCompleter().registerSuggestion("players", (args, sender, command) -> {
                    String last = args.get(args.size() - 1).toLowerCase(Locale.ROOT);
                    return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).filter(s -> s.toLowerCase(Locale.ROOT).startsWith(last)).collect(Collectors.toList());
                }).registerSuggestion("converters", (args, sender, command) -> {
                    String last = args.get(args.size() - 1).toLowerCase(Locale.ROOT);
                    return this.converterManager.getConverters().stream().map(Converter::getName).filter(s -> s.toLowerCase(Locale.ROOT).startsWith(last)).collect(Collectors.toList());
                }).and()
                .register(new EnderChestCommand(this))
                .register(new ConversionCommand(this));

        ((BukkitHandler) commandHandler).registerBrigadier();
    }

    private void setupMetricsAndCheckForUpdate() {
        Metrics metrics = new Metrics(this, 15279);
        UpdateChecker updateChecker = new UpdateChecker(this, 102187);
        updateChecker.getVersion(s -> {
            if (this.getDescription().getVersion().equals(s)) {
                getLogger().info(this.getName() + " is up to date.");
            } else {
                getLogger().info("There is a new update available.");
            }
        });
    }

    @Override
    public void onDisable() {
        this.enderChestManager.finishUp();
        this.dataManager.finishUp();
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public EnderChestManager getEnderChestManager() {
        return enderChestManager;
    }

    public Messages getMessages() {
        return messages;
    }

    public ConverterManager getConverterManager() {
        return converterManager;
    }
}
