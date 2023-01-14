package me.saif.betterenderchests.lang.inventory.packetinterceptor;

import io.netty.channel.*;
import me.saif.betterenderchests.lang.inventory.PacketModifier;
import me.saif.reflectionutils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class PacketInterceptor implements Listener {

    private static final String HANDLER_NAME = "VEC_Interceptor";
    private static final String VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];

    private Method CraftPlayer_getHandle;
    private Field
            EntityPlayer_playerConnection,
            NetworkManager_channel,
            PlayerConnetion_networkManager;

    private PacketModifier modifier;

    public PacketInterceptor(JavaPlugin plugin, PacketModifier modifier) {
        this.modifier = modifier;
        try {
            Class<?> CraftPlayer = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftPlayer");
            CraftPlayer_getHandle = CraftPlayer.getMethod("getHandle");
            Class<?> EntityPlayer = CraftPlayer_getHandle.getReturnType();
            EntityPlayer_playerConnection = Arrays.stream(EntityPlayer.getFields()).filter(field -> field.getType().getName().endsWith("PlayerConnection")).findAny().get();
            Class<?> PlayerConnection = EntityPlayer_playerConnection.getType();
            PlayerConnetion_networkManager = Arrays.stream(PlayerConnection.getFields()).filter(field -> field.getType().getName().endsWith("NetworkManager")).findAny().get();
            Class<?> NetworkManager = PlayerConnetion_networkManager.getType();
            NetworkManager_channel = ReflectionUtils.getFieldsOfType(NetworkManager, Channel.class, true).stream().findFirst().orElse(null);

            plugin.getServer().getPluginManager().registerEvents(this, plugin);

            for (Player t : Bukkit.getOnlinePlayers())
                injectPlayer(t);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
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
                    long start = System.nanoTime();
                    o = modifier.modifyPacket(player, o);
                    long finish = System.nanoTime();
                    long time = finish - start;
                    System.out.println(time);

                    super.write(channelHandlerContext, o, channelPromise);
                }

                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    super.channelRead(ctx, msg);
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

    private void uninjectPlayer(Player p) {
        try {
            Object playerHandle = CraftPlayer_getHandle.invoke(p);
            Object playerConnection = EntityPlayer_playerConnection.get(playerHandle);
            Object networkManager = PlayerConnetion_networkManager.get(playerConnection);
            Channel channel = (Channel) NetworkManager_channel.get(networkManager);
            ChannelPipeline pipe = channel.pipeline();

            if (pipe.names().contains(HANDLER_NAME))
                pipe.remove(HANDLER_NAME);

        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            uninjectPlayer(onlinePlayer);
        }

        HandlerList.unregisterAll(this);
    }

}
