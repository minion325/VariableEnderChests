package me.saif.betterenderchests.utils;

import de.tr7zw.changeme.nbtapi.*;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.utils.DataFixerUtil;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class ItemStackSerializer {

    public static String serialize(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            NBTContainer container = new NBTContainer();
            container.setInteger("size", items.length);
            NBTCompoundList list = container.getCompoundList("items");

            for(int i = 0; i < items.length; ++i) {
                ItemStack item = items[i];
                if (item != null && item.getType() != Material.AIR) {
                    NBTListCompound entry = list.addCompound();
                    entry.setInteger("Slot", i);
                    entry.mergeCompound(NBT.itemStackToNBT(item));
                }
            }
            NBTReflectionUtil.writeApiNBT(container, outputStream);

            outputStream.close();
            return "nbtbytes:" + Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static String serializeJson(ItemStack[] obj) {
        return "json:" + de.tr7zw.changeme.nbtapi.NBT.itemStackArrayToNBT(obj);
    }

    @Deprecated
    public static String serializeOld(ItemStack[] obj) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(obj.length);

            for (int i = 0; i < obj.length; i++) {
                dataOutput.writeObject(obj[i]);
            }

            dataOutput.close();
            outputStream.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static ItemStack[] deserialize(String str) {
        if (str.startsWith("nbtbytes:")) {
            return deserializeNBTBytes(str.substring(9));
        } else if (str.startsWith("json:")) {
            return deserializeJson(str.substring(5));
        } else {
            return deserializeOld(str);
        }
    }

    public static ItemStack[] deserializeNBTBytes(String str) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(str));

            NBTContainer comp = new NBTContainer(NBTReflectionUtil.readNBT(inputStream));

            ItemStack[] rebuild;
            if (!comp.hasTag("size")) {
                rebuild = null;
            } else {
                rebuild = new ItemStack[comp.getInteger("size")];

                for(int i = 0; i < rebuild.length; ++i) {
                    rebuild[i] = new ItemStack(Material.AIR);
                }

                if (!comp.hasTag("items")) {
                    return rebuild;
                } else {
                    NBTCompoundList list = comp.getCompoundList("items");
                    Iterator var3 = list.iterator();

                    while(var3.hasNext()) {
                        ReadWriteNBT lcomp = (ReadWriteNBT)var3.next();
                        if (lcomp instanceof NBTCompound) {
                            int slot = lcomp.getInteger("Slot");

                            if (lcomp.hasTag("Count") && MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
                                lcomp = DataFixerUtil.fixUpItemData(lcomp, 3700, DataFixerUtil.getCurrentVersion());
                            }

                            rebuild[slot] = NBT.itemStackFromNBT(lcomp);
                        }
                    }

                    return rebuild;
                }
            }

            inputStream.close();
            return rebuild;
        } catch (Exception e) {
            return new ItemStack[0];
        }
    }

    public static ItemStack[] deserializeJson(String str) {
        ItemStack[] stacks = NBTItem.convertNBTtoItemArray((NBTCompound) de.tr7zw.changeme.nbtapi.NBT.parseNBT(str));

        if (stacks == null)
            return new ItemStack[0];

        return stacks;
    }


    public static ItemStack[] deserializeOld(String str) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(str));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (Exception e) {
            return new ItemStack[0];
        }
    }


}
