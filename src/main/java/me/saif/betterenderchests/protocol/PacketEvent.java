package me.saif.betterenderchests.protocol;

import org.bukkit.entity.Player;

public class PacketEvent {

    private Player player;
    private Object packet;
    private boolean cancelled;

    public PacketEvent(Player player, Object packet, boolean cancelled) {
        this.player = player;
        this.packet = packet;
        this.cancelled = cancelled;
    }

    public Object getPacket() {
        return packet;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setPacket(Object packet) {
        this.packet = packet;
    }
}
