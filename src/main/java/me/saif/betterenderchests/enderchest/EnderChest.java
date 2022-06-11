package me.saif.betterenderchests.enderchest;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EnderChest implements InventoryHolder {

    private final UUID UUID;
    private final String name;
    private ItemStack[] contents;
    private Inventory inventory;
    private Map<Integer, String> inventoryNames;
    private int lastNumRows = 6;

    protected EnderChest(UUID owner, String name, ItemStack[] contents, Map<Integer, String> inventoryNames) {
        this.UUID = owner;
        this.name = name;
        this.inventoryNames = inventoryNames;
        this.contents = contents.length == 54 ? contents : Arrays.copyOf(contents, 54);
        this.inventory = Bukkit.createInventory(this, lastNumRows * 6, this.inventoryNames.get(lastNumRows));
        populateInventory();
    }

    protected EnderChest(UUID owner, String name, ItemStack[] contents, int lastNumRows, Map<Integer, String> inventoryNames) {
        this(owner, name, contents, inventoryNames);
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
        inventory = Bukkit.createInventory(this, rows * 9, this.inventoryNames.get(rows));
        populateInventory();
        for (HumanEntity viewer : viewers) {
            viewer.openInventory(inventory);
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void clearContents() {
        this.contents = new ItemStack[54];
        this.populateInventory();
    }

    public void setContents(ItemStack[] contents) {
        this.contents = Arrays.copyOf(contents, 54);
        this.populateInventory();
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

    public EnderChestSnapshot snapshot() {
        return new EnderChestSnapshot(this);
    }
}
