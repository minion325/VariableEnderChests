package me.saif.betterenderchests.enderchest;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.data.DataManager;
import me.saif.betterenderchests.data.Messages;
import me.saif.betterenderchests.utils.Callback;
import me.saif.betterenderchests.utils.CaselessString;
import me.saif.betterenderchests.utils.Manager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnderChestManager extends Manager<VariableEnderChests> implements Listener {

    private final DataManager dataManager;

    private final Map<UUID, EnderChest> uuidEnderChestMap = new HashMap<>();
    private final Map<CaselessString, UUID> nameUUIDMap = new HashMap<>();

    private final Map<UUID, Callback<EnderChest>> uuidCallbackMap = new HashMap<>();
    private final Map<CaselessString, Callback<EnderChest>> nameCallbackMap = new HashMap<>();

    private final Map<UUID, Block> openFromBlocks = new HashMap<>();
    private int defaultRows;
    private final boolean convert;

    private final Map<Integer, String> inventoryNames = new HashMap<>();

    private final Sound OPEN_SOUND;
    private final Sound CLOSE_SOUND;

    public EnderChestManager(VariableEnderChests plugin) {
        super(plugin);
        this.dataManager = getPlugin().getDataManager();


        //load the correct sound depending on verison
        if (plugin.getVersion() == 8) {
            OPEN_SOUND = Sound.valueOf("CHEST_OPEN");
            CLOSE_SOUND = Sound.valueOf("CHEST_CLOSE");
        } else if (plugin.getVersion() < 13) {
            OPEN_SOUND = Sound.valueOf("BLOCK_ENDERCHEST_OPEN");
            CLOSE_SOUND = Sound.valueOf("BLOCK_ENDERCHEST_CLOSE");
        } else {
            OPEN_SOUND = Sound.BLOCK_ENDER_CHEST_OPEN;
            CLOSE_SOUND = Sound.BLOCK_ENDER_CHEST_CLOSE;
        }

        //getting config values
        this.convert = this.getPlugin().getConfig().getBoolean("convert-current-ender-chest", true);
        this.defaultRows = this.getPlugin().getConfig().getInt("default-rows", 3);
        for (int i = 1; i <= 6; i++) {
            this.inventoryNames.put(i, ChatColor.translateAlternateColorCodes('&',
                    this.getPlugin().getConfig().getString("enderchest-names." + i + "-rows", "&a<player>'s Enderchest")));
        }
        if (this.defaultRows > 6)
            this.defaultRows = 6;
        else if (this.defaultRows < 0)
            this.defaultRows = 0;

        //load data for already online players eg. if plugin is reloaded.
        Bukkit.getScheduler().runTask(plugin, () -> {


            Set<UUID> toGet = Bukkit.getOnlinePlayers().stream().map((Function<Player, UUID>) Entity::getUniqueId).collect(Collectors.toSet());
            Map<UUID, EnderChestSnapshot> data = this.dataManager.loadEnderChestsByUUID(toGet);

            for (Player player : Bukkit.getOnlinePlayers()) {
                this.dataManager.saveNameAndUUID(player.getName(), player.getUniqueId());

                EnderChestSnapshot snapshot = data.get(player.getUniqueId());
                if (snapshot == null) {
                    this.uuidEnderChestMap.put(player.getUniqueId(),
                            createNew(player));
                } else {
                    this.uuidEnderChestMap.put(player.getUniqueId(),
                            new EnderChest(player.getUniqueId(), player.getName(), snapshot.getContents(), snapshot.getRows(), getInventoryNames(player.getName())));
                }

                if (player.getOpenInventory().getTopInventory().equals(player.getEnderChest())) {
                    player.closeInventory();
                    int rows = this.getNumRows(player);
                    if (rows == 0) {
                        this.getPlugin().getMessages().sendTo(player, Messages.NO_ROWS);
                    }
                    this.openEnderChest(this.getEnderChest(player), player, this.getNumRows(player));
                }
            }
        });

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.getPlugin(), () -> {
            Map<UUID, EnderChestSnapshot> toSave = new HashMap<>();
            Set<UUID> toRemove = new HashSet<>();
            for (EnderChest enderChest : this.uuidEnderChestMap.values()) {
                toSave.put(enderChest.getUUID(), enderChest.snapshot());
                if (Bukkit.getPlayer(enderChest.getUUID()) == null) {
                    toRemove.add(enderChest.getUUID());
                }
            }

            for (UUID uuid : toRemove) {
                this.uuidEnderChestMap.remove(uuid);
            }

            Bukkit.getScheduler().runTaskAsynchronously(this.getPlugin(), () -> this.dataManager.saveEnderChestMultiple(toSave));

            for (CaselessString minecraftName : this.nameCallbackMap.keySet()) {
                Player player = Bukkit.getPlayerExact(minecraftName.getName());
                if (player == null)
                    this.nameCallbackMap.remove(minecraftName);
            }
        }, 6000L, 6000L);
    }

    private EnderChest loadData(String name, UUID uuid) {
        EnderChestSnapshot enderChestSnapshot = this.dataManager.loadEnderChest(uuid);

        if (enderChestSnapshot == null) {
            return null;
        }

        return new EnderChest(uuid, name, enderChestSnapshot.getContents(), enderChestSnapshot.getRows(), getInventoryNames(name));
    }

    @EventHandler
    private void onJoin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED)
            return;

        String name = event.getPlayer().getName();
        UUID uuid = event.getPlayer().getUniqueId();

        this.nameUUIDMap.put(new CaselessString(name), uuid);

        if (this.uuidEnderChestMap.containsKey(uuid)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.getPlugin(), () -> {
            dataManager.saveNameAndUUID(name, uuid);
            EnderChest enderChest = loadData(name, uuid);
            Bukkit.getScheduler().runTask(getPlugin(), () -> uuidEnderChestMap.put(uuid, Objects.requireNonNullElseGet(enderChest,
                    () -> createNew(event.getPlayer()))));
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onInventoryOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        if (event.getClickedBlock().getType() == Material.ENDER_CHEST) {
            event.setCancelled(true);

            int rows = this.getNumRows(player);
            if (rows == 0) {
                this.getPlugin().getMessages().sendTo(player, Messages.NO_ROWS);
            } else {

                EnderChest enderChest = this.getEnderChest(player);
                event.getPlayer().playSound(event.getClickedBlock().getLocation(), OPEN_SOUND, 1, 1F);
                this.openEnderChest(enderChest, player, rows);
                this.openFromBlocks.put(player.getUniqueId(), event.getClickedBlock());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onInventoryClose(InventoryCloseEvent event) {
        if (this.openFromBlocks.containsKey(event.getPlayer().getUniqueId())) {
            event.getViewers().remove(event.getPlayer());

            Block block = this.openFromBlocks.remove(event.getPlayer().getUniqueId());
            ((Player) event.getPlayer()).playSound(block.getLocation(), CLOSE_SOUND, 1, 1F);
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        this.openFromBlocks.remove(event.getPlayer().getUniqueId());

        EnderChest enderChest = getEnderChest(event.getPlayer());

        //remove the player as a viewer
        enderChest.getInventory().getViewers().remove(event.getPlayer());
        event.getPlayer().closeInventory();

        if (!enderChest.hasViewers()) {
            this.getPlugin().getLogger().info("Saving data for " + enderChest.getName());
            EnderChestSnapshot snapshot = enderChest.snapshot();
            this.uuidEnderChestMap.remove(snapshot.getUuid());
            this.nameUUIDMap.remove(new CaselessString(snapshot.getName()));

            Bukkit.getScheduler().runTaskAsynchronously(this.getPlugin(), () -> {
                this.dataManager.saveEnderChest(snapshot.getUuid(), snapshot);
            });
        }

        //someone is watching the enderchest so keep it loaded
        //we will deal with this later when clearing cache periodically.
    }

    public EnderChest getEnderChest(Player player) {
        return this.uuidEnderChestMap.get(player.getUniqueId());
    }

    //opens ender chest and reopens updated enderchest for players
    public void openEnderChest(EnderChest chest, Player player, int rows) {
        chest.setRows(rows);
        this.openEnderChest(chest, player);
    }

    public void openEnderChest(EnderChest chest, Player player) {
        if (Bukkit.getPlayer(chest.getUUID()) != null) {
            chest.openInventory(player);
        } else {
            chest.openInventory(player);
        }
    }

    public void openEnderChest(Player player, int rows) {
        EnderChest chest = this.getEnderChest(player);
        chest.setRows(rows);
        this.openEnderChest(chest, player);
    }

    public void clearEnderChest(EnderChest enderChest) {
        enderChest.clearContents();
    }

    public int getNumRows(Player player) {
        for (int i = 6; i > 0; i--) {
            if (player.hasPermission("enderchest.size." + i)) {
                return i;
            }
        }
        return defaultRows;
    }

    public Callback<EnderChest> getEnderChest(String name) {
        CaselessString mcName = new CaselessString(name);
        if (this.nameUUIDMap.containsKey(mcName) && this.uuidEnderChestMap.get(this.nameUUIDMap.get(mcName)) != null)
            return Callback.withResult(this.uuidEnderChestMap.get(this.nameUUIDMap.get(mcName)));

        if (this.nameCallbackMap.containsKey(mcName))
            return nameCallbackMap.get(mcName);

        Callback<EnderChest> callback = new Callback<>();
        this.nameCallbackMap.put(mcName, callback);

        Bukkit.getScheduler().runTaskAsynchronously(this.getPlugin(), () -> {
            EnderChestSnapshot snapshot = this.dataManager.loadEnderChest(name);

            EnderChest enderChest = snapshot == null ? null : new EnderChest(snapshot.getUuid(), snapshot.getName(), snapshot.getContents(), snapshot.getRows(), getInventoryNames(snapshot.getName()));
            Bukkit.getScheduler().runTask(this.getPlugin(), () -> {
                this.nameCallbackMap.remove(mcName);
                if (enderChest != null) {
                    this.uuidEnderChestMap.put(snapshot.getUuid(), enderChest);
                    this.nameUUIDMap.put(mcName, snapshot.getUuid());
                }
                callback.setResult(enderChest);
            });
        });

        return callback;
    }

    public Callback<EnderChest> getEnderChest(UUID uuid) {
        if (this.uuidEnderChestMap.get(uuid) != null)
            return Callback.withResult(this.uuidEnderChestMap.get(uuid));

        if (this.uuidCallbackMap.containsKey(uuid))
            return uuidCallbackMap.get(uuid);

        Callback<EnderChest> callback = new Callback<>();
        this.uuidCallbackMap.put(uuid, callback);

        Bukkit.getScheduler().runTaskAsynchronously(this.getPlugin(), () -> {
            EnderChestSnapshot snapshot = this.dataManager.loadEnderChest(uuid);

            EnderChest enderChest = snapshot == null ? null : new EnderChest(snapshot.getUuid(), snapshot.getName(), snapshot.getContents(), snapshot.getRows(), getInventoryNames(snapshot.getName()));
            Bukkit.getScheduler().runTask(this.getPlugin(), () -> {
                this.uuidCallbackMap.remove(uuid);
                this.uuidEnderChestMap.put(uuid, enderChest);
                callback.setResult(enderChest);
            });
        });

        return callback;
    }

    public void deleteEnderChest(UUID uuid) {
        EnderChest enderChest = this.uuidEnderChestMap.get(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (enderChest != null) {
            enderChest.clearContents();

            if (player != null) {
                if (this.convert)
                    enderChest.setContents(player.getEnderChest().getContents());
                return;
            }

            //unload enderchest without saving
            this.uuidEnderChestMap.remove(enderChest.getUUID());
        }
        this.dataManager.deleteEnderChest(uuid);
    }

    public void deleteEnderChest(String name) {
        CaselessString caselessString = new CaselessString(name);
        EnderChest enderChest = this.uuidEnderChestMap.get(this.nameUUIDMap.get(caselessString));
        Player player = Bukkit.getPlayerExact(name);
        if (enderChest != null) {
            enderChest.clearContents();

            if (player != null) {
                if (this.convert)
                    enderChest.setContents(player.getEnderChest().getContents());
                return;
            }

            //unload enderchest without saving
            this.uuidEnderChestMap.remove(enderChest.getUUID());
        }
        this.dataManager.deleteEnderChest(name);
    }

    //saves all data
    public void finishUp() {
        for (EnderChest value : this.uuidEnderChestMap.values()) {
            List<HumanEntity> list = new ArrayList<>(value.getInventory().getViewers());
            for (HumanEntity humanEntity : list) {
                humanEntity.closeInventory();
            }
        }
        Map<UUID, EnderChestSnapshot> enderChestSnapshotMap = new HashMap<>();
        for (EnderChest value : this.uuidEnderChestMap.values()) {
            enderChestSnapshotMap.put(value.getUUID(), value.snapshot());
        }
        this.dataManager.saveEnderChestMultiple(enderChestSnapshotMap);

        this.uuidCallbackMap.clear();
        this.nameCallbackMap.clear();
        this.uuidEnderChestMap.clear();
        this.nameUUIDMap.clear();

        enderChestSnapshotMap.clear();
        nameUUIDMap.clear();
    }

    private EnderChest createNew(Player player) {
        return new EnderChest(player.getUniqueId(), player.getName(), this.convert ? player.getEnderChest().getContents() : new ItemStack[]{}, getInventoryNames(player.getName()));
    }

    private Map<Integer, String> getInventoryNames(String name) {
        Map<Integer, String> map = new HashMap<>();
        this.inventoryNames.forEach((integer, s) -> map.put(integer, s.replace("<player>", name)));
        return map;
    }
}
