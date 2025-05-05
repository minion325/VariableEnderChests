package me.saif.betterenderchests.enderchest;

import me.saif.betterenderchests.hooks.ChestSortHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EnderChest implements InventoryHolder {

    public static final Map<Integer, String> INVENTORY_NAMES;
    public static final String PREFIX = "VECEnderChest";
    public static final String RETRIEVAL_NAME = "VECRetrieval;<player>";

    static {
        Map<Integer, String> invNames = new HashMap<>();
        invNames.put(1, PREFIX + ";1;<player>");
        invNames.put(2, PREFIX + ";2;<player>");
        invNames.put(3, PREFIX + ";3;<player>");
        invNames.put(4, PREFIX + ";4;<player>");
        invNames.put(5, PREFIX + ";5;<player>");
        invNames.put(6, PREFIX + ";6;<player>");

        INVENTORY_NAMES = Collections.unmodifiableMap(invNames);
    }

    private final UUID UUID;
    private final String name;
    private ItemStack[] contents;
    private Inventory inventory;
    private final EnderChestRetreiver retrivalHolder = new EnderChestRetreiver();
    private Inventory retrievalInventory;
    private final Map<Integer, String> inventoryNames = new HashMap<>();
    private int lastNumRows = 6;

    protected EnderChest(UUID owner, String name, ItemStack[] contents) {
        this.UUID = owner;
        this.name = name;
        INVENTORY_NAMES.forEach((integer, s) -> {
            this.inventoryNames.put(integer, s.replace("<player>", name));
        });
        this.contents = contents.length == 54 ? contents : Arrays.copyOf(contents, 54);
        this.inventory = Bukkit.createInventory(this, lastNumRows * 9, this.inventoryNames.get(lastNumRows));

        int retrievalSize = 54 - lastNumRows * 9;

        this.retrievalInventory = Bukkit.createInventory(this.retrivalHolder, retrievalSize == 0 ? 9 : retrievalSize, RETRIEVAL_NAME);
        this.retrivalHolder.setInventory(this.retrievalInventory);

        ChestSortHook.setSortable(this.inventory);
        ChestSortHook.setUnsortable(this.retrievalInventory);

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
        List<HumanEntity> chestViewers = new ArrayList<>(this.inventory.getViewers());
        List<HumanEntity> retrievalViewers = new ArrayList<>(this.retrievalInventory.getViewers());
        ChestSortHook.setUnsortable(this.inventory);

        this.inventory = Bukkit.createInventory(this, rows * 9, this.inventoryNames.get(rows));
        ChestSortHook.setSortable(this.inventory);

        int retrievalSize = 54 - lastNumRows * 9;

        this.retrievalInventory = Bukkit.createInventory(this.retrivalHolder, retrievalSize == 0 ? 9 : retrievalSize, RETRIEVAL_NAME);
        this.retrivalHolder.setInventory(this.retrievalInventory);

        ChestSortHook.setUnsortable(this.retrievalInventory);

        populateInventory();
        for (HumanEntity viewer : chestViewers) {
            viewer.openInventory(inventory);
        }

        //reopen the retrieval inventory or close it if there is none anymore
        for (HumanEntity retrievalViewer : retrievalViewers) {
            if (retrievalSize != 0)
                retrievalViewer.openInventory(this.retrivalHolder.getInventory());
            else
                retrievalViewer.closeInventory();
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
        List<HumanEntity> viewers = this.inventory.getViewers();
        List<HumanEntity> retrievalViewers = this.retrievalInventory.getViewers();
        return viewers.isEmpty() || retrievalViewers.isEmpty();
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

        if (inventory.getSize() == 54)
            return;

        for (int i = 0; i < retrievalInventory.getSize(); i++) {
            retrievalInventory.setItem(i, this.contents[i + inventory.getSize()]);
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

        if (inventory.getSize() == 54)
            return;

        for (int i = inventory.getSize(); i < 54; i++) {
            this.contents[i] = this.retrievalInventory.getItem(i - inventory.getSize());
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

    public EnderChestRetreiver getRetriever() {
        return retrivalHolder;
    }

    public static class EnderChestRetreiver implements InventoryHolder {

        private Inventory inventory;

        private EnderChestRetreiver() {
        }

        @NotNull
        @Override
        public Inventory getInventory() {
            return inventory;
        }

        private void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }
}
