package me.saif.betterenderchests.lang.inventory.impl;

import me.saif.betterenderchests.lang.inventory.PacketModifier;
import org.bukkit.Bukkit;

public abstract class OpenEnderchestPacketModifier implements PacketModifier {
    protected final String VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];

}
