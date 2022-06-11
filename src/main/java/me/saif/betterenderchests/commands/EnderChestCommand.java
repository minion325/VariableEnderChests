package me.saif.betterenderchests.commands;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.data.Messages;
import me.saif.betterenderchests.enderchest.EnderChest;
import me.saif.betterenderchests.enderchest.EnderChestManager;
import me.saif.betterenderchests.utils.Callback;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderChestCommand {

    private EnderChestManager ecm;
    private Messages messages;
    private VariableEnderChests plugin;
    private Map<UUID, EnderChest> toClear = new HashMap<>();

    public EnderChestCommand(VariableEnderChests plugin) {
        this.plugin = plugin;
        this.ecm = plugin.getEnderChestManager();
        this.messages = plugin.getMessages();
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
                    messages.sendTo(player, Messages.NO_ROWS);
                return;
            }
            messages.sendTo(player, Messages.COMMAND_NO_PERMISSION_SELF);
            return;
        }

        if (!player.hasPermission("enderchest.command.others")) {
            messages.sendTo(player, Messages.COMMAND_NO_PERMISSION_OTHERS);
            return;
        }
        if (otherPlayer.length() < 3 || otherPlayer.length() > 16) {
            messages.sendTo(player, Messages.NO_ENDERCHEST_FOUND);
        }

        if (Bukkit.getPlayerExact(otherPlayer) != null) {
            Player other = Bukkit.getPlayerExact(otherPlayer);
            EnderChest enderChest = this.ecm.getEnderChest(other);
            int rows = this.ecm.getNumRows(other);
            if (rows == 0) {
                player.sendMessage(ChatColor.RED + other.getName() + " does not have an enderchest!");
                return;
            }
            this.ecm.openEnderChest(enderChest, player, rows);
            return;
        }

        Callback<EnderChest> callback = this.ecm.getEnderChest(otherPlayer);
        callback.addResultListener(() -> {
            EnderChest enderChest = callback.getResult();
            if (enderChest == null) {
                messages.sendTo(player, Messages.NO_ENDERCHEST_FOUND);
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
            messages.sendTo(player, Messages.NO_ENDERCHEST_FOUND);
        }

        if (Bukkit.getPlayerExact(otherPlayer) != null) {
            Player other = Bukkit.getPlayerExact(otherPlayer);
            EnderChest enderChest = this.ecm.getEnderChest(other);
            if (this.toClear.get(player.getUniqueId()) == enderChest) {
                this.ecm.clearEnderChest(enderChest);
                player.sendMessage("Cleared the enderchest of " + other.getName());
                return;
            }
            this.toClear.put(player.getUniqueId(), enderChest);
            player.sendMessage(ChatColor.AQUA + "Are you sure you wish to clear the enderchest of " + other.getName(),
                    ChatColor.GRAY + "Run this command again within 5 seconds to confirm");
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.toClear.remove(player.getUniqueId()), 100L);
            return;
        }

        Callback<EnderChest> callback = this.ecm.getEnderChest(otherPlayer);
        callback.addResultListener(() -> {
            EnderChest enderChest = callback.getResult();
            if (enderChest == null) {
                messages.sendTo(player, Messages.NO_ENDERCHEST_FOUND);
                return;
            }

            if (this.toClear.get(player.getUniqueId()) == enderChest) {
                this.ecm.clearEnderChest(enderChest);
                player.sendMessage("Cleared the enderchest of " + enderChest.getName());
                return;
            }
            this.toClear.put(player.getUniqueId(), enderChest);
            player.sendMessage(ChatColor.AQUA + "Are you sure you wish to clear the enderchest of " + enderChest.getName(),
                    ChatColor.GRAY + "Run this command again within 5 seconds to confirm");
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.toClear.remove(player.getUniqueId()), 100L);
        });
    }


}
