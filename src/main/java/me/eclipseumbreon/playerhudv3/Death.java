package me.eclipseumbreon.playerhudv3;

import me.eclipseumbreon.playerhudv3.playerchest.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Death {

    // Constant Declarations --------------------------------------------------
    private static final PlayerHudV3 plugin = PlayerHudV3.getPlugin();

    private static final Map<UUID, Set<Death>> playerDeathMap = new HashMap<>();

    // Initialization ---------------------------------------------------------
    public static void initialize(){
        String red = EclipseColor.red;
        String reset = EclipseColor.reset;
        String aqua = EclipseColor.aqua;
        for (Player player: Bukkit.getOnlinePlayers()){
            player.sendMessage(red + "meow " + reset + "meow " + aqua + "meow");
        }
    }

    // In-Memory Functions ----------------------------------------------------
    private static void test(){

    }

    private static void addDeath(Death death){
        UUID ownerID = death.getOwnerID();
        Set<Death> deaths = playerDeathMap.getOrDefault(ownerID, new HashSet<>());
        deaths.add(death);
        playerDeathMap.put(ownerID, deaths);
        // write to file
        // write IDs to file
    }

    public static void createNew(Player deadPlayer, List<ItemStack> drops, int exp){
        String timeString = "timeString";
        boolean keepInventory = drops == null;
        //boolean keepExp = exp == -1;

        UUID uuid = deadPlayer.getUniqueId();
        Location location = deadPlayer.getLocation();
        Coordinate coordinate = new Coordinate(uuid, -1, "Death", "death", location, new ItemStack(Material.SKELETON_SKULL));
        PlayerStorage playerStorage = null;
        if (!keepInventory && drops.size() != 0){
            boolean isLarge = drops.size() > 27;
            playerStorage = PlayerStorage.createNewStorage(
                    2, deadPlayer, deadPlayer.getName() + "'s Death", location, isLarge, drops
            );
        }
        addDeath(new Death(uuid,coordinate,playerStorage,exp,timeString));
    }


    // Object-Instanced Functions and Declarations ---------------------------------------------------------------------
    private UUID ownerID;
    private Coordinate coordinate;
    PlayerStorage playerStorage;
    int exp;
    String timeString;

    public Death(UUID ownerID, Coordinate coordinate, PlayerStorage playerStorage, int exp, String timeString){
        this.ownerID = ownerID;
        this.coordinate = coordinate;
        this.playerStorage = playerStorage;
        this.exp = exp;
        this.timeString = timeString;
    }

    public UUID getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(UUID ownerID) {
        this.ownerID = ownerID;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public PlayerStorage getPlayerStorage() {
        return playerStorage;
    }

    public void setPlayerStorage(PlayerStorage playerStorage) {
        this.playerStorage = playerStorage;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }


}
