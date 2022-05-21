package me.saif.betterenderchests.data.commands;

import me.saif.betterenderchests.BetterEnderChests;
import me.saif.betterenderchests.enderchest.EnderChest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Optional;

public class EnderChestCommand {

    private BetterEnderChests plugin;

    public EnderChestCommand(BetterEnderChests plugin) {
        this.plugin = plugin;
    }

    @Command({"echest", "enderchest"})
    @AutoComplete("@players")
    public void openEchest(Player player,@Named("player") @Optional String otherPlayer) {
        EnderChest enderChest = plugin.getEnderChestManager().getEnderChest(otherPlayer == null ? player : Bukkit.getPlayer(otherPlayer));
        this.plugin.getEnderChestManager().openEnderChest(enderChest, player);
    }

}
