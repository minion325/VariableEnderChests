package me.saif.betterenderchests.command.commands;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.command.PluginCommand;
import me.saif.betterenderchests.data.SQLiteDataManager;
import me.saif.betterenderchests.data.database.SQLiteDatabase;
import me.saif.betterenderchests.enderchest.EnderChestSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class EnderChestDebugCommand extends PluginCommand {

    private VariableEnderChests plugin;

    public EnderChestDebugCommand(VariableEnderChests plugin) {
        super("vecdebug");
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender sender, String alias, String[] args) {
        if (args.length == 0) {
            cmdDefault(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("updaterows")) {
            updateRows();
            sender.sendMessage("Updated enderchest rows for all players.");
            return;
        }

        if (args[0].equalsIgnoreCase("paper?")) {
            sender.sendMessage(VariableEnderChests.isPaper() ? "Yes" : "No");
            return;
        }

        if (args[0].equalsIgnoreCase("openbackup")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You need to be a player to use this command!");
                return;
            }

            Player player = ((Player) sender);

            if (args.length < 3) {
                sender.sendMessage("Usage: /vecdebug openbackup <player> <file name>.db");
                return;
            }

            String[] filenameArray = Arrays.copyOfRange(args, 2, args.length);
            String fileName = String.join(" ", filenameArray);
            openBackup(player, args[1], fileName);
            return;
        }

        /*if (args[0].equalsIgnoreCase("dobackup")) {
            boolean success = this.plugin.getDataManager().createBackup(this.plugin.getLogger(), new File());
            if (success)
                sender.sendMessage("Backup complete.");
            else
                sender.sendMessage("Backup failed. See console for more info");
            return;
        }*/

        cmdDefault(sender);
    }

    private void updateRows() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            this.plugin.getEnderChestManager().updateRows(onlinePlayer);
        }
    }

    private void cmdDefault(CommandSender sender) {
        sender.sendMessage("You are running " + this.plugin.getName() + " " + this.plugin.getDescription().getVersion());
    }

    private void openBackup(Player sender, String playerName, String fileName) {
        File pluginFolder = this.plugin.getDataFolder();

        if (!fileName.endsWith(".db")) {
            sender.sendMessage("You need to specify a .db file");
            return;
        }

        if (fileName.equalsIgnoreCase("data.db")) {
            sender.sendMessage("You cannot retrieve data from data.db");
            return;
        }

        File backupFile = new File(pluginFolder, fileName);

        if (!backupFile.exists()) {
            sender.sendMessage("That backup file does not exist in the VariableEnderChest folder");
            return;
        }

        UUID playerUUID = this.plugin.getDataManager().getUUID(playerName);

        if (playerUUID == null) {
            sender.sendMessage("Cannot find UUID for that player.");
            return;
        }

        SQLiteDatabase database = new SQLiteDatabase(pluginFolder, fileName);
        SQLiteDataManager dataManager = new SQLiteDataManager(database);

        EnderChestSnapshot snapshot = dataManager.loadEnderChest(playerUUID);

        if (snapshot == null) {
            sender.sendMessage("No backup data for " + playerName + " found.");
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, 54, playerName + "'s Backup EnderChest");

        inventory.setContents(snapshot.getContents());
        sender.openInventory(inventory);
    }

    @Override
    public String getPermission() {
        return "enderchest.debug";
    }
}
