package me.saif.betterenderchests.lang.inventory.impl;

import me.saif.betterenderchests.lang.inventory.InvMultilangCommons;
import me.saif.betterenderchests.lang.locale.Locale;
import me.saif.betterenderchests.lang.locale.PlayerLocaleFinder;
import me.saif.reflectionutils.ReflectionUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class OpenEnderchestPacketModifier_1_12_Below extends OpenEnderchestPacketModifier {

    private Class<?> OpenWindowPacketClass;

    private Field name;
    private Method Component_getText;
    private Constructor<?> ChatComponentText_init;
    private boolean initialized = false;
    private final PlayerLocaleFinder locale;

    public OpenEnderchestPacketModifier_1_12_Below(PlayerLocaleFinder locale) {
        this.locale = locale;
        try {
            OpenWindowPacketClass = Class.forName("net.minecraft.server." + VERSION + ".PacketPlayOutOpenWindow");

            name = OpenWindowPacketClass.getDeclaredField("c");
            name.setAccessible(true);

            Component_getText = name.getType().getMethod("getText");

            ChatComponentText_init = ReflectionUtils.getClassInPackage(OpenWindowPacketClass.getPackage(), "ChatComponentText").get().getConstructor(String.class);
            initialized = true;
        } catch (Exception e) {
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

            String invName = ((String) Component_getText.invoke(component));

            Map.Entry<String, Integer> ownerSizePair = InvMultilangCommons.parseInventoryName(invName);

            if (ownerSizePair == null)
                return o;

            Locale loc = locale.getLocale(player);

            String newName;

            //it is a retrieval inv
            if (ownerSizePair.getValue() == -1) {
                newName = loc.getSingleFormattedMessage(InvMultilangCommons.RETRIEVAL_NAME, InvMultilangCommons.PLAYER_NAME_PLACEHOLDER.getResult(ownerSizePair.getKey()));
            } else {
                newName = loc.getSingleFormattedMessage(InvMultilangCommons.SIZE_NAME_MAP.get(ownerSizePair.getValue()), InvMultilangCommons.PLAYER_NAME_PLACEHOLDER.getResult(ownerSizePair.getKey()));
            }



            Object newComp = ChatComponentText_init.newInstance(newName);

            name.set(o, newComp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return o;
    }

    @Override
    public boolean canModifyPacket(Object o) {
        return initialized && o.getClass() == OpenWindowPacketClass;
    }
}
