package me.saif.betterenderchests.protocol;

import org.bukkit.entity.Player;

public interface OutboundPacketListener {

    void onSendPacket(PacketEvent event);
}
