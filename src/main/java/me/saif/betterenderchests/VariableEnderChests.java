package me.saif.betterenderchests;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import me.saif.betterenderchests.command.CommandManager;
import me.saif.betterenderchests.command.commands.ClearEnderChestCommand;
import me.saif.betterenderchests.command.commands.ConversionCommand;
import me.saif.betterenderchests.command.commands.EnderChestCommand;
import me.saif.betterenderchests.command.commands.EnderChestDebugCommand;
import me.saif.betterenderchests.converters.ConverterManager;
import me.saif.betterenderchests.data.*;
import me.saif.betterenderchests.data.database.MySQLDatabase;
import me.saif.betterenderchests.data.database.SQLDatabase;
import me.saif.betterenderchests.data.database.SQLiteDatabase;
import me.saif.betterenderchests.enderchest.EnderChestClickListener;
import me.saif.betterenderchests.enderchest.EnderChestManager;
import me.saif.betterenderchests.enderchest.EnderChestRetrieverClickListener;
import me.saif.betterenderchests.hooks.ChestSortHook;
import me.saif.betterenderchests.hooks.InteractiveChatHook;
import me.saif.betterenderchests.hooks.PAPIEnderChestHook;
import me.saif.betterenderchests.hooks.ShowItemHook;
import me.saif.betterenderchests.lang.Messenger;
import me.saif.betterenderchests.lang.inventory.InventoryNameListener_1_20;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public final class VariableEnderChests extends JavaPlugin {

    private static VariableEnderChestAPI API;
    private static final boolean paper;

    static {
        boolean temp;
        try {
            Bukkit.getServer().spigot().getClass().getMethod("getPaperConfig");
            temp = true;
        } catch (NoSuchMethodException e) {
            temp = false;
        }
        paper = temp;
    }

    public static boolean isPaper() {
        return paper;
    }

    public static VariableEnderChestAPI getAPI() {
        return API;
    }

    private DataManager dataManager;
    private EnderChestManager enderChestManager;
    private ConverterManager converterManager;
    private LocaleLoader localeLoader;
    private Messenger messenger;
    private PlayerLocaleFinder playerLocaleFinder;
    private SQLDatabase database;
    private PacketInterceptor packetInterceptor;
    private CommandManager commandManager;
    private PAPIEnderChestHook enderChestHook;

    @Override
    public void onEnable() {
        API = new VariableEnderChestAPI(this);
        this.saveDefaultConfig();

        new ConfigUpdater(this);

        this.localeLoader = new LocaleLoader(this);
        this.playerLocaleFinder = new PlayerLocaleFinder(this);

        if (!MinecraftVersion.isNewerThan(MinecraftVersion.MC1_19_R3)) {
            PacketModifier packetModifier;

            if (!MinecraftVersion.isNewerThan(MinecraftVersion.MC1_12_R1))
                packetModifier = new OpenEnderchestPacketModifier_1_12_Below(this.playerLocaleFinder);
            else if (!MinecraftVersion.isNewerThan(MinecraftVersion.MC1_16_R3))
                packetModifier = new OpenEnderchestPacketModifier_1_16_Below(this.playerLocaleFinder);
            else
                packetModifier = new OpenEnderchestPacketModifier_1_19_Below(this.playerLocaleFinder);

            this.packetInterceptor = new PacketInterceptor(this, packetModifier);
        } else {
            Bukkit.getPluginManager().registerEvents(new InventoryNameListener_1_20(this), this);
        }

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
        setupHooks();
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
            this.dataManager = new MySQLDataManager((MySQLDatabase) this.database);
        } else {
            this.database = new SQLiteDatabase(this.getDataFolder(), "data.db");
            this.dataManager = new SQLiteDataManager((SQLiteDatabase) this.database);
        }
        this.dataManager.init();
    }

    public SQLDatabase getSQLDatabase() {
        return database;
    }

    private void setupEnderChestManager() {
        this.enderChestManager = new EnderChestManager(this);
        Bukkit.getPluginManager().registerEvents(this.enderChestManager, this);
        Bukkit.getPluginManager().registerEvents(new EnderChestClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EnderChestRetrieverClickListener(this), this);

        this.converterManager = new ConverterManager(this);
    }

    private void setupCommands() {
        this.commandManager = new CommandManager(this);

        this.commandManager.registerCommand(new ClearEnderChestCommand(this));
        this.commandManager.registerCommand(new ConversionCommand(this));
        this.commandManager.registerCommand(new EnderChestDebugCommand(this));

        List<String> aliases = this.getConfig().getStringList("open-enderchest-commands");

        String cmdName = aliases.isEmpty() ? "enderchest" : aliases.remove(0);

        this.commandManager.registerCommand(new EnderChestCommand(this, cmdName, aliases));

    }

    private void setupHooks() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.enderChestHook = new PAPIEnderChestHook(this);
            this.enderChestHook.register();
        }

        ChestSortHook.hook();
        new InteractiveChatHook(this);
        new ShowItemHook(this);
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
        if (this.enderChestManager != null)
            this.enderChestManager.finishUp();

        if (this.packetInterceptor != null)
            packetInterceptor.shutdown();

        if (this.commandManager != null)
            this.commandManager.unregisterAll();

        if (this.dataManager != null)
            this.dataManager.finishUp();

        if (this.enderChestHook != null)
            this.enderChestHook.unregister();
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

    public LocaleLoader getLocaleLoader() {
        return localeLoader;
    }

    public PlayerLocaleFinder getPlayerLocaleFinder() {
        return playerLocaleFinder;
    }
}
