package me.saif.betterenderchests.enderchest;

import me.saif.betterenderchests.BetterEnderChests;
import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.utils.Manager;
import me.saif.betterenderchests.utils.MinecraftName;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EnderChestManager extends Manager<BetterEnderChests> implements Listener {

    private DataManager dataManager;

    private Map<UUID, EnderChest> uuidEnderChestMap = new ConcurrentHashMap<>();
    private Map<MinecraftName, UUID> nameUUIDMap = new ConcurrentHashMap<>();
    private Set<UUID> toCreate = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public EnderChestManager(BetterEnderChests plugin) {
        super(plugin);
        this.dataManager = getPlugin().getDataManager();

        //load data for already online players eg. if plugin is reloaded.
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            this.onPlayerLogin(onlinePlayer.getName(), onlinePlayer.getUniqueId());
            this.onJoin(new PlayerJoinEvent(onlinePlayer, ""));
        }
    }

    @EventHandler
    private void onAsyncPlayerLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        onPlayerLogin(event.getName(), event.getUniqueId());
    }

    private void onPlayerLogin(String name, UUID uuid) {
        this.dataManager.saveNameAndUUID(name, uuid);

        this.nameUUIDMap.put(new MinecraftName(name), uuid);

        if (this.uuidEnderChestMap.containsKey(uuid)) {
            //data is loaded :D so we just need to check that name is the same
            return;
        }

        EnderChestSnapshot enderChestSnapshot = this.dataManager.loadEnderChest(uuid);

        if (enderChestSnapshot == null) {
            toCreate.add(uuid);
            return;
        }

        EnderChest enderChest = new EnderChest(uuid, name, enderChestSnapshot.getContents());
        this.uuidEnderChestMap.put(uuid, enderChest);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!toCreate.contains(player.getUniqueId())) return;

        if (this.uuidEnderChestMap.containsKey(player.getUniqueId()))
            throw new IllegalStateException("Cannot create new enderchest for a player with an already loaded enderchest");

        EnderChest enderChest = new EnderChest(player.getUniqueId(), player.getName(), player.getEnderChest().getContents());
        this.uuidEnderChestMap.put(player.getUniqueId(), enderChest);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryOpen(InventoryOpenEvent event) {
        if (event.isCancelled()) return;

        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getInventory().equals(player.getEnderChest())) event.setCancelled(true);

        for (int i = 6; i > 0; i--) {
            if (player.hasPermission("enderchest.size." + i)) {
                openEnderChest(player, i);
                return;
            }
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        EnderChest enderChest = getEnderChest(event.getPlayer());
        event.getPlayer().closeInventory();

        if (isSafeToRemove(enderChest)) {
            EnderChestSnapshot snapshot = enderChest.snapshot();
            this.uuidEnderChestMap.remove(snapshot.getUuid());
            this.nameUUIDMap.remove(new MinecraftName(snapshot.getName()));

            Bukkit.getScheduler().runTaskAsynchronously(this.getPlugin(), () -> {
                this.dataManager.saveEnderChest(snapshot.getUuid(), snapshot);
            });
        }

        //else do nothing, we will deal with this later when clearing cache.
    }

    public EnderChest getEnderChest(Player player) {
        return this.uuidEnderChestMap.get(player.getUniqueId());
    }

    //opens ender chest and reopens updated enderchest for players
    public void openEnderChest(Player player, int rows) {
        EnderChest enderChest = getEnderChest(player);

        Inventory oldChest = enderChest.getInventory();
        Inventory newChest = enderChest.getInventory(rows);

        //if they're the same
        if (oldChest.equals(newChest)) {
            player.openInventory(newChest);
            return;
        }

        //if not we need to open the new one for all viewers as well
        List<HumanEntity> list = oldChest.getViewers();
        if (!list.contains(player))
            list.add(player);

        for (HumanEntity humanEntity : list) {
            humanEntity.closeInventory();
            humanEntity.openInventory(enderChest.getInventory());
        }
    }

    //checks if anyone has the inventory open. if yes then it is not safe to remove
    public boolean isSafeToRemove(EnderChest enderChest) {
        List<HumanEntity> viewers = enderChest.getInventory().getViewers();
        //debug start
        getPlugin().getLogger().info(viewers.size() == 0 ? "Safe to remove" : "Unsafe to remove");
        //debug end
        return viewers.size() == 0;
    }

    //saves data
    //only to be called by the main class
    public void finishUp() {
        Map<UUID, EnderChestSnapshot> enderChestSnapshotMap = new HashMap<>();
        for (EnderChest value : this.uuidEnderChestMap.values()) {
            enderChestSnapshotMap.put(value.getUUID(), value.snapshot());
        }
        this.dataManager.saveEnderChestMultiple(enderChestSnapshotMap);
        enderChestSnapshotMap.clear();
        nameUUIDMap.clear();
    }
}
