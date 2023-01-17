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

import java.util.ArrayList;
import java.util.List;

public class EnderChestCommand extends PluginCommand {

    private final EnderChestManager ecm;
    private final Messenger messenger;
    private final Placeholder<Player> playerPlaceholder = Placeholder.getPlaceholder("player", Player::getName);

    private final String PERMISSION_SELF = "enderchest.command";
    private final String PERMISSION_OTHERS = "enderchest.command.others";

    public EnderChestCommand(VariableEnderChests plugin, String name, List<String> aliases) {
        super(name, aliases.toArray(new String[0]));
        this.ecm = plugin.getEnderChestManager();
        this.messenger = plugin.getMessenger();
    }

    @Override
    public void onCommand(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            messenger.sendMessage(sender, MessageKey.COMMAND_PLAYER_ONLY);
            return;
        }

        Player player = ((Player) sender);

        openEchest(player, args.length == 0 ? null : args[0]);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (sender.hasPermission(PERMISSION_OTHERS) && args.length == 1)
            return null;
        return new ArrayList<>();
    }

    public void openEchest(Player player, String otherPlayer) {
        if (otherPlayer == null) {
            if (player.hasPermission(PERMISSION_SELF)) {
                int rows = this.ecm.getNumRows(player);
                if (rows != 0)
                    this.ecm.openEnderChest(player, rows);
                else
                    messenger.sendMessage(player, MessageKey.NO_ENDERCHEST_SELF);
                return;
            }
            messenger.sendMessage(player, MessageKey.EC_COMMAND_NO_PERMISSION_SELF);
            return;
        }

        if (!player.hasPermission(PERMISSION_OTHERS)) {
            messenger.sendMessage(player, MessageKey.EC_COMMAND_NO_PERMISSION_OTHERS);
            return;
        }
        if (otherPlayer.length() < 3 || otherPlayer.length() > 16) {
            messenger.sendMessage(player, MessageKey.NO_ENDERCHEST_OTHER);
        }

        if (Bukkit.getPlayerExact(otherPlayer) != null) {
            Player other = Bukkit.getPlayerExact(otherPlayer);
            EnderChest enderChest = this.ecm.getEnderChest(other);
            int rows = this.ecm.getNumRows(other);
            if (rows == 0) {
                messenger.sendMessage(player, MessageKey.NO_ENDERCHEST_OTHER, playerPlaceholder.getResult(other));
                return;
            }
            this.ecm.openEnderChest(enderChest, player, rows);
            return;
        }

        Callback<EnderChest> callback = this.ecm.getEnderChest(otherPlayer);
        callback.addResultListener(() -> {
            EnderChest enderChest = callback.getResult();
            if (enderChest == null) {
                messenger.sendMessage(player, MessageKey.NO_ENDERCHEST_OTHER, PlaceholderResult.of("<player>", otherPlayer));
                return;
            }

            this.ecm.openEnderChest(enderChest, player);
        });
    }

}
