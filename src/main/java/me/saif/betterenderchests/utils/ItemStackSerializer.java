package me.saif.betterenderchests.utils;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTReflectionUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ItemStackSerializer {

    public static String serialize(ItemStack[] obj) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            NBTContainer container = NBTItem.convertItemArraytoNBT(obj);
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

            NBTContainer container = new NBTContainer(NBTReflectionUtil.readNBT(inputStream));
            ItemStack[] stacks = NBTItem.convertNBTtoItemArray(container);

            inputStream.close();
            return stacks;
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
