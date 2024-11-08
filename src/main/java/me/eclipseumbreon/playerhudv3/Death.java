package me.eclipseumbreon.playerhudv3;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Death {

    // Constant Declarations --------------------------------------------------
    private static final PlayerHudV3 plugin = PlayerHudV3.getPlugin();

    private static final Map<Player, Set<Death>> playerDeathMap = new HashMap<>();

    // In-Memory Functions ----------------------------------------------------
    private static void test(){

    }

    public static boolean addDeath(Player deadPlayer, List<ItemStack> drops, int exp){
        String timeString = "timeString";
        boolean keepInventory = drops == null;
        boolean keepExp = exp == -1;

        UUID uuid = deadPlayer.getUniqueId();
        Location location = deadPlayer.getLocation();
        Coordinate coordinate = new Coordinate(uuid, -1, "Death", "death", location, new ItemStack(Material.SKELETON_SKULL));
        Death death = new Death(uuid,coordinate,drops,exp,timeString);


        return true;
    }


    // Object-Instanced Functions and Declarations ---------------------------------------------------------------------
    private UUID deadPlayer;
    private Coordinate coordinate;
    List<ItemStack> items;
    int exp;
    String timeString;

    public Death(UUID deadPlayer, Coordinate coordinate, List<ItemStack> items, int exp, String timeString){
        this.deadPlayer = deadPlayer;
        this.coordinate = coordinate;
        this.items = items;
        this.exp = exp;
        this.timeString = timeString;
    }

    public UUID getDeadPlayer() {
        return deadPlayer;
    }

    public void setDeadPlayer(UUID deadPlayer) {
        this.deadPlayer = deadPlayer;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
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
