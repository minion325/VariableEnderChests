package me.saif.betterenderchests.enderchest;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EnderChest {

    private final UUID UUID;
    private final String name;
    private final ItemStack[] contents;
    private Inventory inventory;
    private int lastNumRows = 6;
    private String inventoryName;

    protected EnderChest(UUID owner, String name, ItemStack[] contents) {
        this.UUID = owner;
        this.name = name;
        this.inventoryName = name + "'s Enderchest";
        this.contents = contents.length == 54 ? contents : Arrays.copyOf(contents, 54);
        this.inventory = Bukkit.createInventory(null, 54, this.inventoryName);
        populateInventory();
    }

    protected EnderChest(UUID owner, String name, ItemStack[] contents, int lastNumRows) {
        this(owner, name, contents);
        this.setRows(lastNumRows);
    }

    public void setRows(int rows) {
        if (rows < 1)
            rows = 1;
        else if (rows > 6)
            rows = 6;

        lastNumRows = rows;

        if (inventory.getSize() == rows * 9)
            return;

        //else
        updateContentsArray();
        List<HumanEntity> viewers = new ArrayList<>(this.inventory.getViewers());
        inventory = Bukkit.createInventory(null, rows * 9, this.inventoryName);
        populateInventory();
        for (HumanEntity viewer : viewers) {
            viewer.openInventory(inventory);
        }
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public boolean hasViewers() {
        List<HumanEntity> viewers = this.getInventory().getViewers();
        return viewers.size() > 0;
    }

    protected void openInventory(Player player) {
        if (player.getOpenInventory().getTopInventory().equals(this.inventory))
            return;
        player.openInventory(this.inventory);
    }

    //sets each item in the inventory to what the contents array says it should be
    private void populateInventory() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, this.contents[i]);
        }
    }

    public ItemStack[] getContents() {
        updateContentsArray();
        return Arrays.copyOf(contents, 54);
    }

    //this updates the contents array either because we want to save some data or the inventory is being recreated
    private void updateContentsArray() {
        for (int i = 0; i < inventory.getSize(); i++) {
            this.contents[i] = inventory.getItem(i);
        }
    }

    public int getLastNumRows() {
        return lastNumRows;
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return UUID;
    }

    public void setInventoryName(String name) {
        this.inventoryName = name;
    }

    public String getInventoryName() {
        return this.inventoryName;
    }

    public EnderChestSnapshot snapshot() {
        return new EnderChestSnapshot(this);
    }
}
