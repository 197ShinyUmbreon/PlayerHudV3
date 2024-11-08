package me.eclipseumbreon.playerhudv3.playerchest;

import me.eclipseumbreon.playerhudv3.PlayerHudV3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerStorage {
    private static final PlayerHudV3 plugin = PlayerHudV3.getPlugin();

    public static final NamespacedKey key = new NamespacedKey(plugin,"player_storage");

    private static final Map<UUID, Set<PlayerStorage>> playerStorageMap = new HashMap<>();
    private static final Map<UUID, UUID> playerOpenStorageMap = new HashMap<>();

    public static void initialize(){
        loadPlayerStoragesFromFile();
    }

    // Command Functions -----------------------------------------------------------------------------------------------
    public static List<String> listOfContainerIDs(){
        return listOfContainerIDs(null);
    }
    public static List<String> listOfContainerIDs(UUID targetOwnerID){
        List<String> containerIDStrings = new ArrayList<>();
        Set<UUID> ownerIDs;
        if (targetOwnerID == null){
            ownerIDs = playerStorageMap.keySet();
        }else ownerIDs = Set.of(targetOwnerID);
        for (UUID ownerID:ownerIDs){
            for (PlayerStorage playerStorage:playerStorageMap.get(ownerID)){
                containerIDStrings.add(playerStorage.getContainerID().toString());
            }
        }
        return containerIDStrings;
    }

    // Static Functions ------------------------------------------------------------------------------------------------
    /*
    Load all info except contents on initialize, loading content data only when needed.
     */
    private static void loadPlayerStoragesFromFile(){
        for (OfflinePlayer offlinePlayer:Bukkit.getOfflinePlayers()){
            Set<PlayerStorage> playerStorages = new HashSet<>();
            UUID ownerID = offlinePlayer.getUniqueId();
            File idFile = new File(plugin.getDataFolder() + "/ps/" + ownerID.toString(),"IDs" + ".yml");
            YamlConfiguration idData = YamlConfiguration.loadConfiguration(idFile);
            List<String> containerIDStrings = idData.getStringList("IDs");
            for (String containerIDString:containerIDStrings){
                UUID containerID = UUID.fromString(containerIDString);
                File containerFile = new File(plugin.getDataFolder() + "/ps/" + ownerID.toString(), containerID.toString() + ".yml");
                YamlConfiguration containerData = YamlConfiguration.loadConfiguration(containerFile);

                String title = containerData.getString("title");
                Location location = containerData.getLocation("location");
                boolean isLarge = containerData.getBoolean("isLarge");

                System.out.println("Loaded Player Storage From File:\n\t"  + ownerID + " - " + containerID + " - " + title + " - Is Large: " + isLarge);

                PlayerStorage playerStorage = new PlayerStorage(ownerID,containerID,title,location,isLarge,null);
                playerStorages.add(playerStorage);
            }
            System.out.println("ContainerIDStrings length: " + containerIDStrings.size() + " " + offlinePlayer.getName());
            playerStorageMap.put(ownerID,playerStorages);
        }
        int entries = 0;
        for (UUID ownerID: playerStorageMap.keySet()){
            for (PlayerStorage ignored :playerStorageMap.get(ownerID)){
                entries++;
            }
        }
        System.out.println(
                "Loaded Player Storage(s) from file.\nLoaded " + entries + " entries."
        );
    }

    public static PlayerStorage fetchRandomPlayerStorage(Player player){
        UUID ownerID = player.getUniqueId();
        Set<PlayerStorage> playerStorages = playerStorageMap.getOrDefault(ownerID, new HashSet<>());
        int choices = playerStorages.size();
        if (choices == 0){
            player.sendMessage("No saved storages.");
            return null;
        }
        Random random = new Random();
        int choice = random.nextInt(0, choices);
        List<PlayerStorage> playerStoragesList = new ArrayList<>(playerStorages);
        return playerStoragesList.get(choice);
    }

    public static void testOpenRandomStorage(Player player){
        PlayerStorage playerStorage = fetchRandomPlayerStorage(player);
        if (playerStorage == null) return;
        //UUID containerID = playerStorage.getContainerID();
        //openTargetPlayerStorage(player, containerID);
        playerStorage.openStorage(player);
    }

    private static void writeIDListToFile(UUID ownerID){
        Set<String> idList = new HashSet<>();
        for (PlayerStorage playerStorage:playerStorageMap.get(ownerID)){
            UUID containerID = playerStorage.getContainerID();
            idList.add(containerID.toString());
        }
        File idFile = new File(plugin.getDataFolder() + "/ps/" + ownerID.toString(),"IDs" + ".yml");
        YamlConfiguration idData = YamlConfiguration.loadConfiguration(idFile);
        idData.set("IDs", new ArrayList<String>(idList));
        try{
            idData.save(idFile);
        }catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try{
            idFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PlayerStorage getPlayerStorageFromID(UUID containerID){
        for (UUID ownerID:playerStorageMap.keySet()){
            for (PlayerStorage playerStorage: playerStorageMap.get(ownerID)){
                UUID savedContainerID = playerStorage.getContainerID();
                if (containerID.equals(savedContainerID))return playerStorage;
            }
        }
        // No PlayerChests of matching ID found
        return null;
    }

    public static PlayerStorage createNewStorage(Player player, String title, Location location, boolean isLarge, List<ItemStack> contents){
        UUID containerID = UUID.randomUUID();
        PlayerStorage playerStorage = new PlayerStorage(player.getUniqueId(), containerID, title, location, isLarge, contents);
        addPlayerStorage(playerStorage);
        return playerStorage;
    }

    private static void addPlayerStorage(PlayerStorage playerStorage){
        UUID ownerID = playerStorage.getOwnerID();
        Set<PlayerStorage> playerStorages = playerStorageMap.getOrDefault(ownerID, new HashSet<>());
        playerStorages.add(playerStorage);
        playerStorageMap.put(ownerID, playerStorages);
        playerStorage.writeContentsToFile();
        writeIDListToFile(ownerID);
    }

    public static void openTargetPlayerStorage(Player player, UUID containerID){
        if (playerOpenStorageMap.containsValue(containerID)){
            player.sendMessage("This container is already opened somewhere else.");
            return;
        }
        PlayerStorage playerStorage = getPlayerStorageFromID(containerID);
        if (playerStorage == null){
            player.sendMessage("Container ID Not Found.");
            return;
        }
        playerStorage.openStorage(player);
        //openPlayerStorage(player, playerStorage);
    }

    private static void openPlayerStorage(Player player, PlayerStorage playerStorage){
        UUID containerID = playerStorage.getContainerID();
        int size = 27;
        if (playerStorage.getIsLarge()) size = 54;
        Inventory inv = Bukkit.createInventory(null, size, containerID.toString());
        List<ItemStack> contents = playerStorage.getContents();
        if (contents == null){
            player.sendMessage("Contents of Container was Null.");
            return;
        }
        ItemStack[] contentArray = contents.toArray(new ItemStack[size]);
        inv.setStorageContents(contentArray);
        playerOpenStorageMap.put(player.getUniqueId(), containerID);
        player.openInventory(inv);
    }

    public static void closePlayerStorage(UUID ownerID){
        playerOpenStorageMap.remove(ownerID);
    }


    // Local Functions -------------------------------------------------------------------------------------------------
    public void openStorage(Player player){
        openPlayerStorage(player, this);
    }
    public void loadContentsFromFile(){
        File psFile = new File(plugin.getDataFolder() + "/ps/" + this.ownerID,this.containerID + ".yml");
        YamlConfiguration psData = YamlConfiguration.loadConfiguration(psFile);
        List<ItemStack> contents = (List<ItemStack>) psData.getList("contents"); // ?????????????????????
        if (contents == null){
            System.out.println("Failed to load Contents from File.\nOwner: " + this.ownerID.toString() + "\nContainerID: " + this.containerID.toString());
            return;
        }
//        int size = 27;
//        if (contents.size() > 27) size = 54;
//        ItemStack[] contentArray = new ItemStack[size];
//        int i = 0;
//        for (ItemStack itemStack:contents){
//            contentArray[i] = itemStack;
//            i++;
//        }
        this.contents = contents;
    }

    public void updateContents(List<ItemStack> contents){
        this.contents = contents;
        writeContentsToFile();
    }

    public void writeContentsToFile(){
        if (this.contents == null){
            System.out.println("writeContentsToFile() contents are null. Did something go wrong?");
            return;
        }
        String ownerID = this.getOwnerID().toString();
        String containerID = this.containerID.toString();
        File psFile = new File(plugin.getDataFolder() + "/ps/" + ownerID,containerID + ".yml");
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
            psFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerStorage(UUID ownerID, UUID containerID, String title, Location location, boolean isLarge, List<ItemStack> contents){
        this.ownerID = ownerID;
        this.containerID = containerID;
        this.title = title;
        this.location = location;
        this.isLarge = isLarge;
        this.contents = contents;
    }

    private UUID ownerID;
    private UUID containerID;
    private String title;
    Location location;
    boolean isLarge;
    List<ItemStack> contents;

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
