package me.eclipseumbreon.playerhudv3;

import me.eclipseumbreon.playerhudv3.playerchest.PlayerStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Events implements Listener {

    private static final PlayerHudV3 plugin = PlayerHudV3.getPlugin();
    private static final boolean debug = PlayerHudV3.debug;

    @EventHandler
    public static void testPlayerStorage(InventoryCloseEvent ice){
        if (!debug)return;
        Player player = (Player) ice.getPlayer();
        PlayerStorage.closePlayerStorage(player.getUniqueId());
        InventoryType type = ice.getInventory().getType();
        if (type != InventoryType.CHEST)return;
        Inventory inv = ice.getInventory();
        String title = ice.getView().getTitle();
        UUID containerID = null;
        try{
            containerID = UUID.fromString(title);
        }catch (IllegalArgumentException ignored){

        }
        if (containerID != null){
            PlayerStorage playerStorage = PlayerStorage.getPlayerStorageFromID(containerID);
            if (playerStorage == null){
                player.sendMessage("No matching Storage found with container ID: " + containerID);
                return;
            }
            playerStorage.updateContents(Arrays.asList(inv.getStorageContents()));
            player.sendMessage("Storage updated. ID: " + containerID);
        }else{
            boolean isLarge = inv.getSize() == 54;
            List<ItemStack> contents = new ArrayList<>(Arrays.asList(inv.getStorageContents()));
            PlayerStorage storage = PlayerStorage.createNewStorage(player, "Default Title", null, isLarge, contents);
            player.sendMessage("New Storage Created. ID: " + storage.getContainerID().toString());
        }
    }

    @EventHandler
    public static void testAssignPlayerStorageToContainer(PlayerInteractEvent pie){
        if (!debug)return;
        if (pie.getHand() == null)return;
        if (!pie.getHand().equals(EquipmentSlot.HAND))return;
        if (!pie.getAction().equals(Action.RIGHT_CLICK_BLOCK))return;
        Block storageBlock = pie.getClickedBlock();
        if (storageBlock == null)return;
        if (!storageBlock.getType().equals(Material.BARREL) && !storageBlock.getType().equals(Material.CHEST))return;
        Player player = pie.getPlayer();
        if (!player.getInventory().getItemInMainHand().getType().equals(Material.STICK))return;
        Container storageMeta = (Container) storageBlock.getState();
        PersistentDataContainer data = storageMeta.getPersistentDataContainer();
        PlayerStorage randomStorage = PlayerStorage.fetchRandomPlayerStorage(player);
        if (randomStorage == null)return;
        UUID containerID = randomStorage.getContainerID();
        data.set(PlayerStorage.key, PersistentDataType.STRING, containerID.toString());
        storageMeta.update();
        pie.setCancelled(true);
        player.sendMessage("Wrote storage data to container. ID: " + containerID);
    }

    @EventHandler
    public static void onOpenPlayerStorageContainer(PlayerInteractEvent pie){
        if (pie.getHand() == null)return;
        if (!pie.getHand().equals(EquipmentSlot.HAND))return;
        if (!pie.getAction().equals(Action.RIGHT_CLICK_BLOCK))return;
        Block storageBlock = pie.getClickedBlock();
        if (storageBlock == null)return;
        if (!storageBlock.getType().equals(Material.BARREL) && !storageBlock.getType().equals(Material.CHEST))return;
        Player player = pie.getPlayer();
        Container storageMeta = (Container) storageBlock.getState();
        PersistentDataContainer data = storageMeta.getPersistentDataContainer();
        String containerIDString = data.get(PlayerStorage.key, PersistentDataType.STRING);
        if (containerIDString == null){
            player.sendMessage("No saved PlayerStorage data on this container.");
            return;
        }
        UUID containerID = null;
        try{
            containerID = UUID.fromString(containerIDString);
        }catch (IllegalArgumentException ignored){

        }
        if (containerID == null){
            player.sendMessage("Container ID was null.");
            return;
        }
        PlayerStorage playerStorage = PlayerStorage.getPlayerStorageFromID(containerID);
        if (playerStorage == null){
            player.sendMessage("No matching Player Storage found for given ID: " + containerID);
            return;
        }
        pie.setCancelled(true);
        player.swingMainHand();
        playerStorage.openStorage(player);
    }

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent pje){
        Coordinate.populatePlayerCoordinates(pje.getPlayer());
        Players.addJoinedPlayer(pje.getPlayer());
    }

    @EventHandler
    public static void onPlayerLeave(PlayerQuitEvent pqe){Coordinate.depopulatePlayerCoordinates(pqe.getPlayer());}

    @EventHandler
    public static void onPlayerDeath(PlayerDeathEvent pde){
        Player player = pde.getEntity();
        Location deathPoint = player.getLocation();
        List<ItemStack> drops = pde.getDrops();
        if (pde.getKeepInventory()) drops = null;
        int exp = pde.getDroppedExp();
        if (pde.getKeepLevel()) exp = -1;
        Death.addDeath(player, drops, exp);
//        String biome = player.getWorld().getBiome(deathPoint).name();
//        Coordinate.addCoordinate(player, biome + " Death Point", "death", deathPoint, new ItemStack(Material.SKELETON_SKULL));
    }

}
