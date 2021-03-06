package me.saif.betterenderchests;

import me.saif.betterenderchests.commands.ConversionCommand;
import me.saif.betterenderchests.commands.EnderChestCommand;
import me.saif.betterenderchests.converters.Converter;
import me.saif.betterenderchests.converters.ConverterManager;
import me.saif.betterenderchests.data.ConfigUpdater;
import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.data.Messages;
import me.saif.betterenderchests.data.SQLDataManager;
import me.saif.betterenderchests.data.database.SQLDatabase;
import me.saif.betterenderchests.data.database.SQLiteDatabase;
import me.saif.betterenderchests.enderchest.EnderChestClickListener;
import me.saif.betterenderchests.enderchest.EnderChestManager;
import me.saif.betterenderchests.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.core.BukkitHandler;

import java.util.Locale;
import java.util.stream.Collectors;

public final class VariableEnderChests extends JavaPlugin {

    private static VariableEnderChestAPI API;

    public static VariableEnderChestAPI getAPI() {
        return API;
    }

    private DataManager dataManager;
    private EnderChestManager enderChestManager;
    private Messages messages;
    private ConverterManager converterManager;
    private int version;
    private SQLDatabase database;

    @Override
    public void onEnable() {
        API = new VariableEnderChestAPI(this);
        try {
            this.version = Integer.parseInt(Bukkit.getServer().getClass().getPackageName().split("\\.")[3].split("_")[1]);
        } catch (NumberFormatException e) {
            this.version = 13;
        }

        this.saveDefaultConfig();
        new ConfigUpdater(this);

        this.messages = new Messages(this.getConfig());

        setupDataManager();
        setupEnderChestManager();
        setupCommands();
        setupMetricsAndCheckForUpdate();
    }

    private void setupDataManager() {
        this.database = new SQLiteDatabase(this.getDataFolder(), "data.db");
        this.dataManager = new SQLDataManager(this);
        this.dataManager.init();
    }

    public SQLDatabase getDatabase() {
        return database;
    }

    private void setupEnderChestManager() {
        this.enderChestManager = new EnderChestManager(this);
        Bukkit.getPluginManager().registerEvents(this.enderChestManager, this);
        Bukkit.getPluginManager().registerEvents(new EnderChestClickListener(), this);

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

    public int getVersion() {
        return version;
    }
}
