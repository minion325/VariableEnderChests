package me.saif.betterenderchests.lang.inventory.impl;

import me.saif.betterenderchests.lang.inventory.InvMultilangCommons;
import me.saif.betterenderchests.lang.locale.Locale;
import me.saif.betterenderchests.lang.locale.PlayerLocaleFinder;
import me.saif.reflectionutils.ReflectionUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class OpenEnderchestPacketModifier_1_16_Below extends OpenEnderchestPacketModifier {

    private final PlayerLocaleFinder locale;
    private boolean initialized = false;
    private Field name;
    private Class<?> OpenWindowPacketClass;
    private Method ChatComponent_getString;
    private Method ChatComponent_static_fromString;


    public OpenEnderchestPacketModifier_1_16_Below(PlayerLocaleFinder locale) {
        this.locale = locale;
        try {
            OpenWindowPacketClass = Class.forName("net.minecraft.server." + VERSION + ".PacketPlayOutOpenWindow");

            name = OpenWindowPacketClass.getDeclaredField("c");
            name.setAccessible(true);

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

            Map.Entry<String, Integer> ownerSizePair = InvMultilangCommons.parseInventoryName(invName);

            if (ownerSizePair == null)
                return o;

            Locale loc = locale.getLocale(player);

            String newName = loc.getSingleFormattedMessage(InvMultilangCommons.SIZE_NAME_MAP.get(ownerSizePair.getValue()), InvMultilangCommons.PLAYER_NAME_PLACEHOLDER.getResult(ownerSizePair.getKey()));

            Object newComp = ChatComponent_static_fromString.invoke(null, ComponentSerializer.toString(TextComponent.fromLegacyText(newName)));

            name.set(o, newComp);
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
