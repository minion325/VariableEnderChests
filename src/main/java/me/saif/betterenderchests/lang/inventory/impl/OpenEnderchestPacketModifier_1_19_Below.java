package me.saif.betterenderchests.lang.inventory.impl;

import me.saif.betterenderchests.lang.locale.Locale;
import me.saif.betterenderchests.lang.locale.PlayerLocale;
import me.saif.reflectionutils.ReflectionUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class OpenEnderchestPacketModifier_1_19_Below extends OpenEnderchestPacketModifier {

    private final PlayerLocale locale;
    private boolean initialized = false;
    private Field name, id;
    private Class<?> OpenWindowPacketClass;
    private Method ChatComponent_getString, ChatComponent_static_fromString, OpenWindow_getContainer;
    private Constructor<?> OpenWindow_init;


    public OpenEnderchestPacketModifier_1_19_Below(PlayerLocale locale) {
        this.locale = locale;
        try {
            OpenWindowPacketClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutOpenWindow");
            OpenWindow_init = Arrays.stream(OpenWindowPacketClass.getConstructors()).filter(constructor -> constructor.getParameterTypes().length == 3).findAny().get();

            name = OpenWindowPacketClass.getDeclaredField("c");
            name.setAccessible(true);

            id = OpenWindowPacketClass.getDeclaredField("a");
            id.setAccessible(true);

            OpenWindow_getContainer = ReflectionUtils.getPublicMethodsOfType(OpenWindowPacketClass, OpenWindow_init.getParameterTypes()[1]).stream().findFirst().get();

            ChatComponent_getString = name.getType().getMethod("getString");

            ChatComponent_static_fromString = ReflectionUtils.getInnerClass(name.getType(), "ChatSerializer").get().getMethod("a", String.class);

            initialized = true;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object modifyPacket(Player player, Object o) {
        if (!initialized)
            return o;
        try {
            if (!canModifyPacket(o))
                return o;

            Object component = name.get(o);

            String invName = ((String) ChatComponent_getString.invoke(component));

            Map.Entry<String, Integer> ownerSizePair = parseInventoryName(invName);

            if (ownerSizePair == null)
                return o;

            Locale loc = locale.getLocale(player);

            String newName = loc.getSingleFormattedMessage(SIZE_NAME_MAP.get(ownerSizePair.getValue()), PLAYER_NAME_PLACEHOLDER.getResult(ownerSizePair.getKey()));

            Object newComp = ChatComponent_static_fromString.invoke(null, ComponentSerializer.toString(TextComponent.fromLegacyText(newName)));

            o = OpenWindow_init.newInstance(id.get(o), OpenWindow_getContainer.invoke(o), newComp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    @Override
    public boolean canModifyPacket(Object o) {
        return o.getClass() == OpenWindowPacketClass;
    }
}
