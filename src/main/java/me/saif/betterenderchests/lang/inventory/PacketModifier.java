package me.saif.betterenderchests.lang.inventory;

import org.bukkit.entity.Player;

public interface PacketModifier {

    Object modifyPacket(Player player, Object o);

    boolean canModifyPacket(Object o);

    class EnderChestInfo {
        private String name;
        private int rows;

        public EnderChestInfo(String name, int rows) {
            this.name = name;
            this.rows = rows;
        }

        public String getName() {
            return name;
        }

        public int getRows() {
            return rows;
        }
    }

}
