package me.saif.betterenderchests.data.backups;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.utils.Manager;

import java.io.File;

public class BackupManager extends Manager<VariableEnderChests> {

    private File backupsFolder;

    public BackupManager(VariableEnderChests plugin) {
        super(plugin);
        this.backupsFolder = new File(plugin.getDataFolder(), "backups");
    }




}
