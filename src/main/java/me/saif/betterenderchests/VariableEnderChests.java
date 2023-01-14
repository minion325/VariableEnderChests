package me.saif.betterenderchests;

import me.saif.betterenderchests.commands.ConversionCommand;
import me.saif.betterenderchests.commands.EnderChestCommand;
import me.saif.betterenderchests.converters.Converter;
import me.saif.betterenderchests.converters.ConverterManager;
import me.saif.betterenderchests.data.ConfigUpdater;
import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.data.SQLDataManager;
import me.saif.betterenderchests.data.database.MySQLDatabase;
import me.saif.betterenderchests.data.database.SQLDatabase;
import me.saif.betterenderchests.data.database.SQLiteDatabase;
import me.saif.betterenderchests.enderchest.EnderChestClickListener;
import me.saif.betterenderchests.enderchest.EnderChestManager;
import me.saif.betterenderchests.lang.Messenger;
import me.saif.betterenderchests.lang.inventory.PacketModifier;
import me.saif.betterenderchests.lang.inventory.impl.OpenEnderchestPacketModifier_1_12_Below;
import me.saif.betterenderchests.lang.inventory.impl.OpenEnderchestPacketModifier_1_16_Below;
import me.saif.betterenderchests.lang.inventory.impl.OpenEnderchestPacketModifier_1_19_Below;
import me.saif.betterenderchests.lang.inventory.packetinterceptor.PacketInterceptor;
import me.saif.betterenderchests.lang.locale.LocaleLoader;
import me.saif.betterenderchests.lang.locale.PlayerLocaleFinder;
import me.saif.betterenderchests.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.core.BukkitHandler;

import java.io.File;
import java.sql.SQLException;
import java.util.Locale;
import java.util.stream.Collectors;

public final class VariableEnderChests extends JavaPlugin {

    private static VariableEnderChestAPI API;

    public static VariableEnderChestAPI getAPI() {
        return API;
    }

    private static final int getMCVersion;


    static {
        int temp;
        try {
            temp = Integer.parseInt(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].split("_")[1]);
        } catch (NumberFormatException e) {
            temp = 13;
        }
        getMCVersion = temp;

    }

    private DataManager dataManager;
    private EnderChestManager enderChestManager;
    private ConverterManager converterManager;
    private LocaleLoader localeLoader;
    private Messenger messenger;
    private PlayerLocaleFinder playerLocaleFinder;
    private SQLDatabase database;
    private PacketInterceptor packetInterceptor;

    @Override
    public void onEnable() {
        API = new VariableEnderChestAPI(this);
        this.saveDefaultConfig();

        new ConfigUpdater(this);

        this.localeLoader = new LocaleLoader(this);
        this.playerLocaleFinder = new PlayerLocaleFinder(this);

        PacketModifier packetModifier;

        if (getMCVersion() <= 12)
            packetModifier = new OpenEnderchestPacketModifier_1_12_Below(this.playerLocaleFinder);
        else if (getMCVersion() <= 16)
            packetModifier = new OpenEnderchestPacketModifier_1_16_Below(this.playerLocaleFinder);
        else
            packetModifier = new OpenEnderchestPacketModifier_1_19_Below(this.playerLocaleFinder);

        this.packetInterceptor = new PacketInterceptor(this, packetModifier);

        this.messenger = new Messenger(this.localeLoader, this.playerLocaleFinder);

        try {
            setupDataManager();
        } catch (SQLException e) {
            e.printStackTrace();
            this.getLogger().severe("Could not connect to the database. Disabling plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        setupEnderChestManager();
        setupCommands();
        setupMetricsAndCheckForUpdate();
    }

    private void setupDataManager() throws SQLException {
        if (getConfig().getBoolean("database.mysql", false)) {
            this.database = new MySQLDatabase(
                    getConfig().getString("database.host"),
                    getConfig().getInt("database.port"),
                    getConfig().getString("database.database"),
                    getConfig().getString("database.username"),
                    getConfig().getString("database.password"));
        } else {
            this.database = new SQLiteDatabase(this.getDataFolder(), "data.db");
        }
        this.dataManager = new SQLDataManager(this);
        this.dataManager.init();
    }

    public SQLDatabase getSQLDatabase() {
        return database;
    }

    private void setupEnderChestManager() {
        this.enderChestManager = new EnderChestManager(this);
        Bukkit.getPluginManager().registerEvents(this.enderChestManager, this);
        Bukkit.getPluginManager().registerEvents(new EnderChestClickListener(this), this);

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

        if (((BukkitCommandHandler) commandHandler).isBrigadierSupported())
            ((BukkitCommandHandler) commandHandler).registerBrigadier();
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
        this.packetInterceptor.shutdown();
        this.enderChestManager.finishUp();
        this.dataManager.finishUp();
    }


    public DataManager getDataManager() {
        return dataManager;
    }

    public EnderChestManager getEnderChestManager() {
        return enderChestManager;
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public File getPluginFile() {
        return getFile();
    }

    public ConverterManager getConverterManager() {
        return converterManager;
    }

    public static int getMCVersion() {
        return getMCVersion;
    }

    public LocaleLoader getLocaleLoader() {
        return localeLoader;
    }

    public PlayerLocaleFinder getPlayerLocaleFinder() {
        return playerLocaleFinder;
    }
}
