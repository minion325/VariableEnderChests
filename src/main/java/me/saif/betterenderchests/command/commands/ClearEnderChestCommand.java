package me.saif.betterenderchests.command.commands;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.command.PluginCommand;
import me.saif.betterenderchests.enderchest.EnderChest;
import me.saif.betterenderchests.enderchest.EnderChestManager;
import me.saif.betterenderchests.lang.MessageKey;
import me.saif.betterenderchests.lang.Messenger;
import me.saif.betterenderchests.lang.placeholder.Placeholder;
import me.saif.betterenderchests.lang.placeholder.PlaceholderResult;
import me.saif.betterenderchests.utils.Callback;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ClearEnderChestCommand extends PluginCommand {

    private final UUID consoleUUID = new UUID(0, 0);
    private final Map<UUID, EnderChest> toClear = new HashMap<>();
    private final Placeholder<EnderChest> enderChestPlaceholder = Placeholder.getPlaceholder("player", EnderChest::getName);
    private final Placeholder<String> usagePlaceholder = Placeholder.getStringPlaceholder("command");
    private final EnderChestManager ecm;
    private final Messenger messenger;
    private VariableEnderChests plugin;

    private final String PERMISSION = "enderchest.clear";

    public ClearEnderChestCommand(VariableEnderChests plugin) {
        super("clearenderchest", "clearechest");
        this.plugin = plugin;
        this.ecm = plugin.getEnderChestManager();
        this.messenger = plugin.getMessenger();
    }

    @Override
    public void onCommand(CommandSender sender, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            messenger.sendMessage(sender, MessageKey.EC_COMMAND_NO_PERMISSION_SELF);
            return;
        }

        if (args.length == 0) {
            PlaceholderResult result = usagePlaceholder.getResult((sender instanceof Player ? "/" : "") + alias + " <player>");
            messenger.sendMessage(sender, MessageKey.COMMAND_USAGE, result);
            return;
        }

        clearEchest(sender, args[0]);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION) || args.length == 1)
            return super.onTabComplete(sender, alias, args);

        return new ArrayList<>();
    }

    public void clearEchest(CommandSender sender, String otherPlayer) {
        if (otherPlayer.length() < 3 || otherPlayer.length() > 16) {
            messenger.sendMessage(sender, MessageKey.NO_ENDERCHEST_OTHER, PlaceholderResult.of("<player>", otherPlayer));
        }

        if (Bukkit.getPlayerExact(otherPlayer) != null) {
            Player other = Bukkit.getPlayerExact(otherPlayer);
            EnderChest enderChest = this.ecm.getEnderChest(other);

            commonClearEnderChest(sender, enderChest);

            return;
        }

        Callback<EnderChest> callback = this.ecm.getEnderChest(otherPlayer);
        callback.addResultListener(() -> {
            EnderChest enderChest = callback.getResult();
            if (enderChest == null) {
                messenger.sendMessage(sender, MessageKey.NO_ENDERCHEST_OTHER);
                return;
            }

            commonClearEnderChest(sender, enderChest);
        });
    }

    private void commonClearEnderChest(CommandSender sender, EnderChest enderChest) {
        UUID senderUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : consoleUUID;
        if (this.toClear.get(senderUUID) == enderChest) {
            this.ecm.clearEnderChest(enderChest);
            messenger.sendMessage(sender, MessageKey.CLEARED_ENDERCHEST, enderChestPlaceholder.getResult(enderChest));
            return;
        }
        this.toClear.put(senderUUID, enderChest);
        messenger.sendMessage(sender, MessageKey.CONFIRM_CLEAR_ENDERCHEST, enderChestPlaceholder.getResult(enderChest));
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.toClear.remove(senderUUID), 100L);
    }

}
