package me.saif.betterenderchests;

import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.data.SQLiteDataManager;
import me.saif.betterenderchests.enderchest.EnderChestManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterEnderChests extends JavaPlugin {

    private DataManager dataManager;
    private EnderChestManager enderChestManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        setupDataManager();
        this.enderChestManager = new EnderChestManager(this);
    }

    private void setupDataManager() {
        this.dataManager = new SQLiteDataManager(this);
        this.dataManager.init();
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
}
