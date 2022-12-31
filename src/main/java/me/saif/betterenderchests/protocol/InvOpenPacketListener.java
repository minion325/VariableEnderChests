package me.saif.betterenderchests.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class InvOpenPacketListener implements OutboundPacketListener {

    @Override
    public void onSendPacket(PacketEvent event) {
        try {
            Object packet = event.getPacket();
            if (packet.getClass().getSimpleName().equals("PacketPlayOutOpenWindow")) {

                Constructor<?> con = null;
                for (Constructor<?> constructor : packet.getClass().getConstructors()) {
                    if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].getSimpleName().equals("PacketDataSerializer"))
                        con = constructor;
                }

                Constructor<?> pds = con.getParameterTypes()[0].getConstructor(ByteBuf.class);
                Object dataSerializer = pds.newInstance(Unpooled.buffer());

                Method serialize = null;
                for (Method method : packet.getClass().getMethods()) {
                    if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(pds.getDeclaringClass()))
                        serialize = method;
                }

                serialize.invoke(packet, dataSerializer);

                ByteBuf buf = ((ByteBuf) dataSerializer);

                readPacket(buf);

                /*int totalLength = readVarInt(buf);

                System.out.println("Length = " + totalLength);

                int packetId = readVarInt(buf);

                System.out.println("Packet ID = " + packetId);*/

                //ByteBuf packetBuffer = buf.readBytes(buf.array().length);

                int windownID = readVarInt(buf);
                int windowType = readVarInt(buf);

                ByteBuf newDS = (ByteBuf) pds.newInstance(Unpooled.buffer());

                writeVarInt(newDS, windownID);
                writeVarInt(newDS, windowType);
                BaseComponent[] component = new ComponentBuilder().bold(true).appendLegacy(ChatColor.RED + "Coool enderChest :)").create();
                byte[] newMsgByteArray = ComponentSerializer.toString(component).getBytes(StandardCharsets.UTF_8);
                writeVarInt(newDS, newMsgByteArray.length);
                newDS.writeBytes(newMsgByteArray);

                readPacket(newDS);

                Object newPacket = con.newInstance(newDS);
                event.setPacket(newPacket);

                    /*Field field = packet.getClass().getDeclaredField("c");
                    field.setAccessible(true);
                    Object title = field.get(packet);
                    System.out.println(Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer").getMethod("a", Class.forName("net.minecraft.network.chat.IChatBaseComponent")).invoke(null, title));*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    public int readVarInt(ByteBuf buf) {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = buf.readByte();
            value |= (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;

            if (position >= 32) throw new RuntimeException("VarInt is too big");
        }

        return value;
    }

    public void writeVarInt(ByteBuf buf, int value) {
        while (true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                buf.writeByte(value);
                return;
            }

            buf.writeByte((value & SEGMENT_BITS) | CONTINUE_BIT);

            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
        }
    }

    public void readPacket(ByteBuf buf) {
        int windownID = readVarInt(buf);

        System.out.println("Window ID = " + windownID);

        int windowType = readVarInt(buf);

        System.out.println("Window Type = " + windowType);

        int mesLength = readVarInt(buf);

        System.out.println("Msg length = " + mesLength);

        String msg = buf.readCharSequence(mesLength, StandardCharsets.UTF_8).toString();

        System.out.println("Message = " + msg);

        buf.resetReaderIndex();
    }

}
