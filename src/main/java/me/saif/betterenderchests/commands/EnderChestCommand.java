package me.saif.betterenderchests.commands;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.data.Messages;
import me.saif.betterenderchests.enderchest.EnderChest;
import me.saif.betterenderchests.enderchest.EnderChestManager;
import me.saif.betterenderchests.utils.Callback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Optional;

public class EnderChestCommand {

    private EnderChestManager ecm;
    private Messages messages;

    public EnderChestCommand(VariableEnderChests plugin) {
        this.ecm = plugin.getEnderChestManager();
        this.messages = plugin.getMessages();
    }

    @Command({"echest", "enderchest"})
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
            this.ecm.openEnderChest(enderChest, player, this.ecm.getNumRows(other));
            return;
        }

        Callback<EnderChest> callback = this.ecm.getEnderChest(otherPlayer);
        callback.addResultListener(() -> {
            EnderChest enderChest = callback.getResult();
            if (enderChest == null ) {
                messages.sendTo(player, Messages.NO_ENDERCHEST_FOUND);
                return;
            }

            this.ecm.openEnderChest(enderChest, player);
        });
    }

}
