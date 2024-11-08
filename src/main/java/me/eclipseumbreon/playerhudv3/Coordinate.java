package me.eclipseumbreon.playerhudv3;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import static me.eclipseumbreon.playerhudv3.PlayerHudV3.*;

public class Coordinate {

    // Static Functions ------------------------------------------------------------------------------------------------
    public static Coordinate clone(Coordinate coordinate){
        return new Coordinate(coordinate.owner, coordinate.number, coordinate.name, coordinate.context,  coordinate.location, coordinate.icon);
    }
    // Constant Declarations --------------------------------------------------
    private static final PlayerHudV3 plugin = PlayerHudV3.getPlugin();
    private static final Map<Player, List<Coordinate>> coordinateMap = new HashMap<>();
    private static final Map<Player, Coordinate> coordinateTrackMap = new HashMap<>();
    private static final Map<Player, Coordinate> coordinateShareReceiveMap = new HashMap<>();

    private static final int maxCoordinates = 5;
    private static final Set<String> contextTypes = Set.of("saved", "death", "tracking");

    // Public Functions --------------------------------------------------------
    public static void initialize(){
        for (Player player: Bukkit.getOnlinePlayers()) populatePlayerCoordinates(player);
    }
    public static void populatePlayerCoordinates(Player player){ //PlayerJoinServer, Plugin Initialize
        loadUserCoordinateStorageFromFile(player);
        repopulatePlayerTrackingCoordinate(player);
    }
    public static void depopulatePlayerCoordinates(Player player){ //PlayerLeaveServer
        coordinateMap.remove(player);
    }
    public static List<Coordinate> getAllPlayerCoordinates(Player player){
        return coordinateMap.getOrDefault(player, List.of());
    }
    public static List<String> getContextualPlayerCoordinateNames(Player player, String context){
        List<String> names = new ArrayList<>();
        for (Coordinate coordinate:getContextualPlayerCoordinates(player, context)) names.add(coordinate.getName());
        return names;
    }
    public static List<Coordinate> getContextualPlayerCoordinates(Player player, String context){
        List<Coordinate> coordinates = new ArrayList<>();
        for (Coordinate coordinate:getAllPlayerCoordinates(player)){
            if (coordinate.getContext().equalsIgnoreCase(context)) coordinates.add(coordinate);
        }
        return coordinates;
    }
    public static Coordinate getCoordinateByName(Player player, String context, String name){
        for (Coordinate coordinate:getContextualPlayerCoordinates(player,context)){
            if (coordinate.getName().equalsIgnoreCase(name))return coordinate;
        }
        return null;
    }
    public static boolean addCoordinate(Coordinate coordinate){
        Player player = Bukkit.getPlayer(coordinate.owner);
        if (player == null)return false;
        return addCoordinate(player, coordinate.name, coordinate.context, coordinate.location, coordinate.icon);
    }
    public static boolean addCoordinate(Player player, String name, String context, Location location, ItemStack icon){
        UUID uuid = player.getUniqueId();
        //String stringUUID = uuid.toString();
        int amount = getContextualPlayerCoordinates(player, context).size();//file.getInt(stringUUID + "." + context + ".amount", 0);
        int number = amount + 1;
        if (number > maxCoordinates) return false;
        if (coordinateNameExists(player, context, name)){
            name = name + getNextNameEnumeration(player,context,name);
        }
        Coordinate coordinate = new Coordinate(uuid,number,name,context,location,icon);
        addCoordinateToMemory(player, coordinate);
        //saveCoordinateToFile(coordinate);
        updateUserCoordinateStorageFile(player);
        return true;
    }
    public static boolean addCurrentCoordinates(Player player, String context, String name){
        Location location = player.getLocation();
        location.add(0,0.6,0);
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        location.setPitch(0.0f);
        return addCoordinate(player, name, context, location, new ItemStack(Material.MAP));
    }
    public static void removeCoordinate(Player player, Coordinate coordinate){
        List<Coordinate> coordinates = getAllPlayerCoordinates(player);
        coordinates.remove(coordinate);
        setPlayerCoordinates(player, sequentializeCoordinateList(coordinates));
    }

    // In-Memory Functions -----------------------------------------------------
    private static boolean coordinateNameExists(Player player, String context, String name){
        for (Coordinate coordinate:getContextualPlayerCoordinates(player, context)){
            if (coordinate.getName().equalsIgnoreCase(name))return true;
        }
        return false;
    }
    private static int getNextNameEnumeration(Player player, String context, String name){
        List<String> matches = new ArrayList<>();
        String normalizedName = name.toUpperCase();
        for (Coordinate coordinate:getContextualPlayerCoordinates(player, context)){
            String normalizedSearch = coordinate.getName().toUpperCase();
            if (normalizedSearch.contains(normalizedName)) matches.add(normalizedSearch);
        }
        //if (matches.size() == 1 && matches.get(0).length() == normalizedName.length()) return 1;
        Set<Integer> taken = new HashSet<>();
        for (String match:matches){
            String search = match.substring(name.length());
            try{
                Integer i = Integer.parseInt(search);
                taken.add(i);
            }catch (NumberFormatException ignored){}
        }
        for (int i = 1; i < 100; i++) {
            if (taken.contains(i))continue;
            return i;
        }
        return -1;
    }
    private static void addCoordinateToMemory(Player player, Coordinate coordinate){
        List<Coordinate> coordinates = coordinateMap.get(player);
        coordinates.add(coordinate);
        coordinateMap.put(player, coordinates);
    }
    private static List<Coordinate> sequentializeCoordinateList(List<Coordinate> coordinates){
        Set<String> contexts = new HashSet<>();
        for (Coordinate coordinate:coordinates) contexts.add(coordinate.context);
        List<List<Coordinate>> lists = new ArrayList<>();
        for (String context:contexts){
            List<Coordinate> shareContext = new ArrayList<>();
            for (Coordinate coordinate:coordinates){
                if (context.matches(coordinate.context)) shareContext.add(coordinate);
            }
            lists.add(shareContext);
        }
        List<Coordinate> sequentialized = new ArrayList<>();
        for (List<Coordinate> list:lists){
            int i = 1;
            for (Coordinate coordinate:list){
                coordinate.setNumber(i);
                i++;
                sequentialized.add(coordinate);
            }
        }
        return sequentialized;
    }
    private static void setPlayerCoordinates(Player player, List<Coordinate> coordinates){
        //removeAllPlayerCoordinatesFromFile(player);
        //saveCoordinatesToFile(coordinates);
        coordinateMap.put(player, coordinates);
        updateUserCoordinateStorageFile(player);
    }
    private static void repopulatePlayerTrackingCoordinate(Player player){
        for (Coordinate coordinate: coordinateMap.get(player)){
            if (coordinate.getContext().matches("tracking")){
                coordinateTrackMap.put(player, coordinate);
                return;
            }
        }
        if (debug) System.out.println(player.getName() + " had no tracking coordinate saved.");
    }
    // Temp Space --------------------------------------------------------------



    // File Functions -----------------------------------------------------------
    private static void updateUserCoordinateStorageFile(Player player){
        String uuid = player.getUniqueId().toString();
        File csFile = new File(plugin.getDataFolder() + "/cs",uuid + ".yml");
        YamlConfiguration csData = new YamlConfiguration();
        List<Coordinate> csList = coordinateMap.getOrDefault(player, new ArrayList<>());
        for (Coordinate coordinate:csList){
            int count = getContextualPlayerCoordinates(player, coordinate.getContext()).size();
            int number = coordinate.getNumber();
            String context = coordinate.getContext();
            String name = coordinate.getName();
            Location location = coordinate.getLocation();
            ItemStack icon = coordinate.getIcon();

            csData.set(context + ".amount", count); //int
            String home = context + "." + number + ".";
            csData.set(home + "name", name); //String
            csData.set(home + "location", location); //Location
            csData.set(home + "icon", icon); //ItemStack
        }
        Coordinate tracking = coordinateTrackMap.get(player);
        if (tracking == null){
            csData.set("tracking", null);
        }else{
            String context = tracking.getContext();
            String name = tracking.getName();
            Location location = tracking.getLocation();
            ItemStack icon = tracking.getIcon();
            String home = "tracking" + ".";
            csData.set(home + "context", context); //String
            csData.set(home + "name", name); //String
            csData.set(home + "location", location); //Location
            csData.set(home + "icon", icon); //ItemStack
        }
        Coordinate sharedReceived = coordinateShareReceiveMap.get(player); // ?????
        if (sharedReceived == null){
            csData.set("sharedReceived", null);
        }else{
            String context = sharedReceived.getContext();
            String name = sharedReceived.getName();
            Location location = sharedReceived.getLocation();
            ItemStack icon = sharedReceived.getIcon();
            String home = "sharedReceived" + ".";
            csData.set(home + "context", context); //String
            csData.set(home + "name", name); //String
            csData.set(home + "location", location); //Location
            csData.set(home + "icon", icon); //ItemStack
        }
        try{
            csData.save(csFile);
        }catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try{
            csFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadUserCoordinateStorageFromFile(Player player){
        String uuid = player.getUniqueId().toString();
        File csFile = new File(plugin.getDataFolder() + "/cs",uuid + ".yml");
        YamlConfiguration storageData = YamlConfiguration.loadConfiguration(csFile);
        List<Coordinate> coordinates = new ArrayList<>();
        for (String context:contextTypes){
            if (context.matches("saved") || context.matches("death")){
                int amount = storageData.getInt(context + ".amount",0);
                for (int i = 1; i <= amount; i++) {
                    String home = context + "." + i;
                    String name = storageData.getString(home + ".name");
                    Location location = storageData.getLocation(home + ".location");
                    ItemStack icon = storageData.getItemStack(home + ".icon");
                    Coordinate coordinate = new Coordinate(player.getUniqueId(), i, name, context, location, icon);
                    coordinates.add(coordinate);
                }
            }else if (context.matches("tracking")){
                String name = storageData.getString(context + ".name");
                if (name == null)continue;
                Location location = storageData.getLocation(context + ".location");
                ItemStack icon = storageData.getItemStack(context + ".icon");
                Coordinate coordinate = new Coordinate(player.getUniqueId(), 1, name, context, location, icon);
                coordinates.add(coordinate);
            }
        }
        coordinateMap.put(player, coordinates);
    }

    // Object-Instanced Functions and Declarations ---------------------------------------------------------------------
    private UUID owner;
    private int number;
    private String name;
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
    public void setOwner(UUID owner){
        this.owner = owner;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name = name;
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
