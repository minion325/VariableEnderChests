package me.saif.betterenderchests;

import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.data.Messages;
import me.saif.betterenderchests.data.SQLiteDataManager;
import me.saif.betterenderchests.commands.EnderChestCommand;
import me.saif.betterenderchests.enderchest.EnderChestManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.core.BukkitHandler;

import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class VariableEnderChests extends JavaPlugin {

    private DataManager dataManager;
    private EnderChestManager enderChestManager;
    private Messages messages;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.messages = new Messages(this.getConfig());

        setupDataManager();
        setupEnderChestManager();
        setupCommands();
    }

    private void setupDataManager() {
        this.dataManager = new SQLiteDataManager(this);
        this.dataManager.init();
    }

    private void setupEnderChestManager() {
        this.enderChestManager = new EnderChestManager(this);
        Bukkit.getPluginManager().registerEvents(this.enderChestManager, this);
    }

    private void setupCommands() {
        CommandHandler commandHandler = new BukkitHandler(this)
                .getAutoCompleter().registerSuggestion("players", (args, sender, command) -> {
                    String last = args.get(args.size() - 1).toLowerCase(Locale.ROOT);
                    return Bukkit.getOnlinePlayers().stream().map((Function<Player, String>) HumanEntity::getName).filter(s -> s.toLowerCase(Locale.ROOT).startsWith(last)).collect(Collectors.toList());
                }).and()
                .register(new EnderChestCommand(this));

        ((BukkitHandler) commandHandler).registerBrigadier();
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
}
