package me.saif.betterenderchests.commands;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.enderchest.EnderChest;
import me.saif.betterenderchests.enderchest.EnderChestManager;
import me.saif.betterenderchests.lang.MessageKey;
import me.saif.betterenderchests.lang.Messenger;
import me.saif.betterenderchests.lang.placeholder.Placeholder;
import me.saif.betterenderchests.lang.placeholder.PlaceholderResult;
import me.saif.betterenderchests.utils.Callback;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class EnderChestCommand {

    private final EnderChestManager ecm;
    private final Messenger messenger;
    private final VariableEnderChests plugin;
    private final Map<UUID, EnderChest> toClear = new HashMap<>();
    private final Placeholder<Player> playerPlaceholder = Placeholder.getPlaceholder("player", Player::getName);
    private final Placeholder<EnderChest> enderChestPlaceholder = Placeholder.getPlaceholder("player", EnderChest::getName);

    public EnderChestCommand(VariableEnderChests plugin) {
        this.plugin = plugin;
        this.ecm = plugin.getEnderChestManager();
        this.messenger = plugin.getMessenger();
    }

    @Command({"echest", "enderchest", "ec"})
    @AutoComplete("@players")
    public void openEchest(Player player, @Named("player") @Optional String otherPlayer) {
        if (otherPlayer == null) {
            if (player.hasPermission("enderchest.command")) {
                int rows = this.ecm.getNumRows(player);
                if (rows != 0)
                    this.ecm.openEnderChest(player, rows);
                else
                    messenger.sendMessage(player, MessageKey.NO_ENDERCHEST_SELF);
                return;
            }
            messenger.sendMessage(player, MessageKey.COMMAND_NO_PERMISSION_SELF);
            return;
        }

        if (!player.hasPermission("enderchest.command.others")) {
            messenger.sendMessage(player, MessageKey.COMMAND_NO_PERMISSION_OTHERS);
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

    @Command({"clearechest", "clearenderchest"})
    @CommandPermission("enderchest.clear")
    @AutoComplete("@players")
    public void clearEchest(Player player, @Named("player") String otherPlayer) {
        if (otherPlayer.length() < 3 || otherPlayer.length() > 16) {
            messenger.sendMessage(player, MessageKey.NO_ENDERCHEST_OTHER, PlaceholderResult.of("<player>", otherPlayer));
        }

        if (Bukkit.getPlayerExact(otherPlayer) != null) {
            Player other = Bukkit.getPlayerExact(otherPlayer);
            EnderChest enderChest = this.ecm.getEnderChest(other);

            commonClearEnderChest(player, enderChest);

            return;
        }

        Callback<EnderChest> callback = this.ecm.getEnderChest(otherPlayer);
        callback.addResultListener(() -> {
            EnderChest enderChest = callback.getResult();
            if (enderChest == null) {
                messenger.sendMessage(player, MessageKey.NO_ENDERCHEST_OTHER);
                return;
            }

            commonClearEnderChest(player, enderChest);
        });
    }

    private void commonClearEnderChest(Player player, EnderChest enderChest) {
        if (this.toClear.get(player.getUniqueId()) == enderChest) {
            this.ecm.clearEnderChest(enderChest);
            messenger.sendMessage(player, MessageKey.CLEARED_ENDERCHEST, enderChestPlaceholder.getResult(enderChest));
            return;
        }
        this.toClear.put(player.getUniqueId(), enderChest);
        messenger.sendMessage(player, MessageKey.CONFIRM_CLEAR_ENDERCHEST, enderChestPlaceholder.getResult(enderChest));
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.toClear.remove(player.getUniqueId()), 100L);
    }


}
