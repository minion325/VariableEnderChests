package me.saif.betterenderchests.protocol;

import io.netty.channel.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketInterceptor implements Listener {

    private static final String HANDLER_NAME = "playground_injector";
    private static final String VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];

    private Method CraftPlayer_getHandle;
    private Field
            EntityPlayer_playerConnection,
            NetworkManager_channel,
            PlayerConnetion_networkManager;

    private final List<InboundPacketListener> inboundListeners = new ArrayList<>();
    private final List<OutboundPacketListener> outboundListeners = new ArrayList<>();

    public PacketInterceptor(JavaPlugin plugin) {
        try {
            Class<?> CraftPlayer = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftPlayer");
            CraftPlayer_getHandle = CraftPlayer.getMethod("getHandle");
            Class<?> EntityPlayer = CraftPlayer_getHandle.getReturnType();
            EntityPlayer_playerConnection = Arrays.stream(EntityPlayer.getFields()).filter(field -> field.getType().getName().endsWith("PlayerConnection")).findAny().get();
            Class<?> PlayerConnection = EntityPlayer_playerConnection.getType();
            PlayerConnetion_networkManager = Arrays.stream(PlayerConnection.getFields()).filter(field -> field.getType().getName().endsWith("NetworkManager")).findAny().get();
            Class<?> NetworkManager = PlayerConnetion_networkManager.getType();
            NetworkManager_channel = getFieldOfType(NetworkManager, Channel.class);

            plugin.getServer().getPluginManager().registerEvents(this, plugin);

            for (Player t : Bukkit.getOnlinePlayers())
                injectPlayer(t);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private Field getFieldOfType(Class<?> clazz, Class<?> type) {
        for (Field field : clazz.getFields()) {
            if (field.getType() == type)
                return field;
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == type)
                return field;
        }

        return null;
    }

    public void addPacketListener(InboundPacketListener listener) {
        this.inboundListeners.add(listener);
    }

    public void addPacketListener(OutboundPacketListener listener) {
        this.outboundListeners.add(listener);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        injectPlayer(e.getPlayer());
    }

    private void injectPlayer(Player p) {
        try {
            Object playerHandle = CraftPlayer_getHandle.invoke(p);
            Object playerConnection = EntityPlayer_playerConnection.get(playerHandle);
            Object networkManager = PlayerConnetion_networkManager.get(playerConnection);
            Channel channel = (Channel) NetworkManager_channel.get(networkManager);
            ChannelPipeline pipe = channel.pipeline();

            if (pipe.names().contains(HANDLER_NAME))
                pipe.remove(HANDLER_NAME);

            ChannelDuplexHandler handler = new ChannelDuplexHandler() {

                private final Player player = p;

                @Override
                public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {
                    PacketEvent event = new PacketEvent(player, o, false);
                    for (OutboundPacketListener outboundListener : outboundListeners) {
                        outboundListener.onSendPacket(event);
                    }

                    if (event.isCancelled())
                        return;
                    super.write(channelHandlerContext, event.getPacket(), channelPromise);
                }

                @Override
                public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                    PacketEvent event = new PacketEvent(player, o, false);
                    for (InboundPacketListener inboundListener : inboundListeners) {
                        inboundListener.onReceivePacket(event);
                    }

                    if (event.isCancelled())
                        return;
                    super.channelRead(channelHandlerContext, o);
                }

            };

            if (pipe.names().contains("packet_handler"))
                pipe.addBefore("packet_handler", HANDLER_NAME, handler);
            else
                pipe.addLast(HANDLER_NAME, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
