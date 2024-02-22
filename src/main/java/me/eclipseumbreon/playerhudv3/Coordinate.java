package me.eclipseumbreon.playerhudv3;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.sql.Array;
import java.util.*;

public class Coordinate {

    private static final PlayerHudV3 plugin = PlayerHudV3.getPlugin();
    private static final FileConfiguration file = plugin.getCoordinateFileConfig();

    private static final Map<Player, List<Coordinate>> coordinateMap = new HashMap<>();
    private static List<Coordinate> getPlayerCoordinates(Player player){
        return coordinateMap.getOrDefault(player, new ArrayList<>());
    }
    private static void setPlayerCoordinates(Player player, List<Coordinate> coordinates){
        coordinateMap.put(player, coordinates);
    }


    private static final int maxCoordinates = 5;
    private static void addCoordinate(Player player, String name, String context, Location location, ItemStack icon){
        UUID uuid = player.getUniqueId();
        String stringUUID = uuid.toString();
        int amount = file.getInt(stringUUID + "." + context + ".amount", 0);
        int number = amount + 1;
        if (number > maxCoordinates){
            player.sendMessage("Too many points saved.");
            return;
        }
        Coordinate coordinate = new Coordinate(uuid,number,name,context,location,icon);
        saveCoordinateToFile(coordinate);
    }
    public static void addCurrentCoordinates(Player player, String context, String name){
        Location location = player.getLocation();
        location.add(0,0.2,0);
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();
        location.setX(blockX + 0.5);
        location.setY(blockY + 0.2);
        location.setZ(blockZ + 0.5);
        addCoordinate(player, name, context, location, new ItemStack(Material.COMPASS));
    }
    private static void saveCoordinatesToFile(List<Coordinate> coordinates){
        for (Coordinate coordinate:coordinates) saveCoordinateToFile(coordinate);
    }
    private static void saveCoordinateToFile(Coordinate coordinate){
        saveCoordinateToFile(coordinate.number, coordinate.owner, coordinate.name, coordinate.context, coordinate.location, coordinate.icon);
    }
    private static void saveCoordinateToFile(int number, UUID uuid, String name, String context, Location location, ItemStack icon){
        String stringUUID = uuid.toString();
        file.set(stringUUID + "." + context + ".amount", number); //int
        String home = stringUUID + "." + context + "." + number + ".";
        file.set(home + "name", name); //String
        file.set(home + "location", location); //Location
        file.set(home + "icon", icon); //ItemStack
        plugin.saveCoordinateFile();
    }
    public static List<Object> getCoordinates(Player player){
        List<Object> list = new ArrayList<>();
        // do shit here
        return list;
    }

    private final UUID owner;
    private final int number;
    private final String name;
    private final String context;
    private final Location location;
    private final ItemStack icon;

    public Coordinate(UUID owner, int number, String name, String context, Location location, ItemStack icon){
        this.owner = owner;
        this.number = number;
        this.name = name;
        this.context = context;
        this.location = location;
        this.icon = icon;
    }

    public UUID getOwner() {
        return owner;
    }
    public int getNumber() {
        return number;
    }
    public String getName() {
        return name;
    }
    public String getContext(){
        return context;
    }
    public Location getLocation(){
        return location;
    }
    public ItemStack getIcon() {
        return icon;
    }
}
