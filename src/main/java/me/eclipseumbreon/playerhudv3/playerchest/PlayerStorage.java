package me.eclipseumbreon.playerhudv3.playerchest;

import me.eclipseumbreon.playerhudv3.PlayerHudV3;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerStorage {
    private static final PlayerHudV3 plugin = PlayerHudV3.getPlugin();

    public static final NamespacedKey key = new NamespacedKey(plugin,"player_storage");

    private static final Set<PlayerStorage> allPlayerStorages = new HashSet<>();
    private static final Map<UUID, Set<UUID>> playerContainerIDMap = new HashMap<>();
    private static final Map<UUID, UUID> playerOpenStorageMap = new HashMap<>();

    public static void initialize(){
        loadPlayerStoragesFromFile();
    }

    // Test & Debug Functions ------------------------------------------------------------------------------------------
    public static void testOpenRandomStorage(Player player){
        PlayerStorage playerStorage = fetchRandomPlayerStorage(player);
        if (playerStorage == null) return;
        playerStorage.open(player);
    }
    public static PlayerStorage fetchRandomPlayerStorage(Player player){
        UUID ownerID = player.getUniqueId();
        List<UUID> containerIDs = new ArrayList<>(playerContainerIDMap.getOrDefault(ownerID, new HashSet<>()));
        int choices = containerIDs.size();
        if (choices == 0){
            player.sendMessage("No saved storages.");
            return null;
        }
        Random random = new Random();
        int choice = random.nextInt(0, choices);
        UUID chosenID = containerIDs.get(choice);
        return getPlayerStorageFromID(chosenID);
    }

    // Static Functions ------------------------------------------------------------------------------------------------

    // Command Functions -----------------------------------------------------------
    public static List<String> listOfContainerIDs(){
        return listOfContainerIDs(null);
    }
    public static List<String> listOfContainerIDs(UUID targetOwnerID){
        List<String> containerIDStrings = new ArrayList<>();
        Set<UUID> ownerIDs;
        if (targetOwnerID == null){
            ownerIDs = playerContainerIDMap.keySet();
        }else ownerIDs = Set.of(targetOwnerID);
        for (UUID ownerID:ownerIDs){
            for (UUID containerID: playerContainerIDMap.get(ownerID)){
                containerIDStrings.add(containerID.toString());
            }
        }
        return containerIDStrings;
    }

    // Static File Handling ---------------------------------------------------------------------

    //Load all info except contents on initialize, loading content data only when needed.
    private static void loadPlayerStoragesFromFile(){
        for (OfflinePlayer offlinePlayer:Bukkit.getOfflinePlayers()){
            Set<PlayerStorage> playerStorages = new HashSet<>();
            Set<UUID> containerIDs = new HashSet<>();
            UUID ownerID = offlinePlayer.getUniqueId();
            String ownerIDString = ownerID.toString();
            System.out.println("Loading Player Storage files for user " + offlinePlayer.getName());
            File folder = new File(plugin.getDataFolder() + "/ps/" + ownerIDString + "/");
            File[] files = folder.listFiles();
            if (files == null){
                continue;
            }
            for (File containerFile:files){
                String containerIDString = containerFile.getName().replace(".yml", "");
                System.out.println("containerIDString parsed from file: " + containerIDString);
                UUID containerID;
                try{
                    containerID = UUID.fromString(containerIDString);
                }catch (IllegalArgumentException ignored){
                    System.out.println("Found an invalid file while loading Player Storages: '" + containerFile.getName() +"'");
                    continue;
                }
                YamlConfiguration containerData = YamlConfiguration.loadConfiguration(containerFile);
                int type = containerData.getInt("type");
                if (type == 0){type = 1;}
                String title = containerData.getString("title");
                Location location = containerData.getLocation("location");
                boolean isLarge = containerData.getBoolean("isLarge");

                System.out.println("Loaded Player Storage From File:\n\t"  + ownerID + " - " + containerID + " - " + title + " - Is Large: " + isLarge);

                PlayerStorage playerStorage = new PlayerStorage(type,ownerID,containerID,title,location,isLarge,null);
                playerStorages.add(playerStorage);
                containerIDs.add(containerID);
            }
            System.out.println("ContainerIDStrings length: " + playerStorages.size() + " " + offlinePlayer.getName());
            allPlayerStorages.addAll(playerStorages);
            playerContainerIDMap.put(ownerID,containerIDs);
        }
        int entries = 0;
        for (UUID ownerID: playerContainerIDMap.keySet()){
            for (UUID ignored : playerContainerIDMap.get(ownerID)){
                entries++;
            }
        }
        System.out.println(
                "Loaded Player Storage(s) from file.\nLoaded " + entries + " entries."
        );
    }
    public static boolean removeFromFile(PlayerStorage playerStorage){
        UUID ownerID = playerStorage.getOwnerID();
        UUID containerID = playerStorage.getContainerID();
        String parent = plugin.getDataFolder() + "/ps/" + ownerID;
        String child = containerID + ".yml";
        File psFile = new File(parent,child);
        return psFile.delete();
    }

    // General Functions -----------------------------------------------------------------
    public static PlayerStorage getPlayerStorageFromID(UUID containerID){
        for (PlayerStorage playerStorage:allPlayerStorages){
            UUID scannedID = playerStorage.getContainerID();
            if (containerID.equals(scannedID))return playerStorage;
        }
        return null;
    }
    public static PlayerStorage createNewStorage(int type, Player player, String title, Location location, boolean isLarge, List<ItemStack> contents){
        UUID containerID = UUID.randomUUID();
        PlayerStorage playerStorage = new PlayerStorage(type, player.getUniqueId(), containerID, title, location, isLarge, contents);
        addPlayerStorage(playerStorage);
        return playerStorage;
    }

    private static void addPlayerStorage(PlayerStorage playerStorage){
        UUID ownerID = playerStorage.getOwnerID();
        UUID containerID = playerStorage.getContainerID();
        allPlayerStorages.add(playerStorage);
        Set<UUID> containerIDs = playerContainerIDMap.getOrDefault(ownerID, new HashSet<>());
        containerIDs.add(containerID);
        playerContainerIDMap.put(ownerID, containerIDs);
        playerStorage.writeContentsToFile();
        //writeIDListToFile(ownerID);
    }

    private static boolean removePlayerStorage(PlayerStorage playerStorage){
        UUID ownerID = playerStorage.getOwnerID();
        UUID containerID = playerStorage.getContainerID();
        allPlayerStorages.remove(playerStorage);
        Set<UUID> containerIDs = playerContainerIDMap.getOrDefault(ownerID, new HashSet<>());
        containerIDs.remove(containerID);
        playerContainerIDMap.put(ownerID, containerIDs);
        return removeFromFile(playerStorage);
    }

    private static boolean isPlayerStorageOpen(PlayerStorage playerStorage){
        return isPlayerStorageOpen(playerStorage.getContainerID());
    }
    private static boolean isPlayerStorageOpen(UUID containerID){
        return playerOpenStorageMap.containsValue(containerID);
    }
    private static boolean openPlayerStorage(Player player, PlayerStorage playerStorage){
        UUID containerID = playerStorage.getContainerID();
        if (isPlayerStorageOpen(containerID)){
            player.sendMessage("This container is already opened somewhere else.");
            return false;
        }
        int size = 54;
        if (!playerStorage.getIsLarge()) size = 27;
        Inventory inv = Bukkit.createInventory(null, size, containerID.toString());
        List<ItemStack> contents = playerStorage.getContents();
        if (contents == null){
            player.sendMessage("Contents of Container was Null.");
            return false;
        }
        ItemStack[] contentArray = contents.toArray(new ItemStack[size]);
        inv.setStorageContents(contentArray);
        playerOpenStorageMap.put(player.getUniqueId(), containerID);
        player.openInventory(inv);
        return true;
    }

    public static void closePlayerStorage(UUID ownerID){
        playerOpenStorageMap.remove(ownerID);
    }

    // Local Functions -------------------------------------------------------------------------------------------------
    private boolean isPlayerStorageOpen(){
        return isPlayerStorageOpen(this);
    }
    public boolean exportToBundle(Player target, boolean removeStorage){
        ItemStack bundle = new ItemStack(Material.BUNDLE);
        BundleMeta bundleMeta = (BundleMeta) bundle.getItemMeta();
        assert bundleMeta != null;
        List<ItemStack> contents = this.getContents();
        if (contents == null){
            target.sendMessage("Contents of storage were Null.");
            return false;
        }
        List<ItemStack> bundleContents = new ArrayList<>();
        for (ItemStack item:contents){
            if (item == null || item.getType().equals(Material.AIR))continue;
            bundleContents.add(item);
        }
        if (bundleContents.size() == 0){
            target.sendMessage("Contents of storage were empty.");
            return false;
        }
        bundleMeta.setItems(bundleContents);
        bundle.setItemMeta(bundleMeta);
        for (ItemStack drop:target.getInventory().addItem(bundle).values()){
            target.getWorld().dropItem(target.getLocation(),drop);
        }
        if (removeStorage) this.remove();
        return true;
    }
    public void remove(){
        removePlayerStorage(this);
    }
    public boolean open(Player player){
        return openPlayerStorage(player, this);
    }

    public void clearContentsFromMemory(){
        this.contents = null;
    }

    public void updateContents(List<ItemStack> contents){
        this.contents = contents;
        writeContentsToFile();
    }

    // Local File Handling -------------------------------------------------------
    public void loadContentsFromFile(){
        File psFile = new File(plugin.getDataFolder() + "/ps/" + this.ownerID,this.containerID + ".yml");
        YamlConfiguration psData = YamlConfiguration.loadConfiguration(psFile);
        List<ItemStack> contents = (List<ItemStack>) psData.getList("contents"); // ?????????????????????
        if (contents == null){
            System.out.println("Failed to load Contents from File.\nOwner: " + this.ownerID.toString() + "\nContainerID: " + this.containerID.toString());
            return;
        }
        this.contents = contents;
    }
    public void writeContentsToFile(){
        if (this.contents == null){
            System.out.println("writeContentsToFile() contents are null. Did something go wrong?");
            return;
        }
        String ownerID = this.getOwnerID().toString();
        String containerID = this.containerID.toString();
        String parent = plugin.getDataFolder() + "/ps/" + ownerID;
        String child = containerID + ".yml";
        File psFile = new File(parent,child);
        YamlConfiguration psData = new YamlConfiguration();
        psData.set("title", this.title);
        psData.set("location", this.location);
        psData.set("isLarge", this.isLarge);
        psData.set("contents", this.contents);
        try{
            psData.save(psFile);
        }catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try{
            if (psFile.createNewFile()){
                PlayerHudV3.cOut(parent + "/" + child + "\n\tPlayer Storage file created.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Declarations, Getters & Setters -------------------------------------------------
    public PlayerStorage(int type, UUID ownerID, UUID containerID, String title, Location location, boolean isLarge, List<ItemStack> contents){
        this.type = type;
        this.ownerID = ownerID;
        this.containerID = containerID;
        this.title = title;
        this.location = location;
        this.isLarge = isLarge;
        this.contents = contents;
    }

    /*
    Type 0 == null, replaced with 1.
    Type 1 == standard player inventory
    Type 2 == player death storage
     */
    private int type;
    private UUID ownerID;
    private UUID containerID;
    private String title;
    Location location;
    boolean isLarge;
    List<ItemStack> contents;

    public int getType(){
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public UUID getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(UUID ownerID) {
        this.ownerID = ownerID;
    }

    public UUID getContainerID() {
        return containerID;
    }

    public void setContainerID(UUID containerID) {
        this.containerID = containerID;
    }

    public String getTitle() {
        return title;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean getIsLarge() {
        return isLarge;
    }

    public void setIsLarge(boolean isLarge) {
        this.isLarge = isLarge;
    }

    public List<ItemStack> getContents() {
        if (contents == null){
            loadContentsFromFile();
        }
        return contents;
    }

    public void setContents(List<ItemStack> contents) {
        this.contents = contents;
    }
}
