package me.eclipseumbreon.playerhudv3;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Players {

    private static final PlayerHudV3 plugin = PlayerHudV3.getPlugin();

    public static void initialize(){
        loadPlayersFromFile();
    }

    private static final Map<UUID,String> joinedPlayersMap = new HashMap<>();
    public static Map<UUID,String> getJoinedPlayersMap(){
        return joinedPlayersMap;
    }
    public static Set<UUID> getJoinedPlayerUUIDs(){
        return joinedPlayersMap.keySet();
    }
    public static Set<String> getJoinedPlayerNames(){
        return new HashSet<>(joinedPlayersMap.values());
    }

    public static UUID getPlayerUUIDFromName(String name){
        for (UUID uuid: joinedPlayersMap.keySet()){
            String savedName = joinedPlayersMap.get(uuid);
            if (savedName.matches(name))return uuid;
        }
        // UUID not found
        return null;
    }

    public static String getPlayerNameFromUUID(UUID uuid){
        return joinedPlayersMap.get(uuid);
    }

    public static void addJoinedPlayer(Player player){
        String playerName = player.getName();
        UUID uuid = player.getUniqueId();
        String savedName = joinedPlayersMap.get(uuid);
        if (savedName != null && savedName.matches(playerName))return;
        String name = player.getName();
        joinedPlayersMap.put(uuid,name);
        writePlayersToFile();
    }

    private static void loadPlayersFromFile(){
        File pFile = new File(plugin.getDataFolder() + "/p","joinedPlayers" + ".yml");
        YamlConfiguration pData = YamlConfiguration.loadConfiguration(pFile);

        List<String> playerUUIDStrings = pData.getStringList("playerUUIDStrings");
        List<String> playerNames = pData.getStringList("playerNames");

        List<UUID> playerUUIDs = new ArrayList<>();
        for (String stringUUID:playerUUIDStrings){
            UUID playerUUID = UUID.fromString(stringUUID);
            playerUUIDs.add(playerUUID);
        }

        int i = 0;
        for (UUID playerUUID:playerUUIDs){
            String playerName = playerNames.get(i);
            joinedPlayersMap.put(playerUUID, playerName);
            i++;
        }

//        pData.getOfflinePlayer();
//        OfflinePlayer player = ;
//        Bukkit.player
    }

    private static void writePlayersToFile(){
        File pFile = new File(plugin.getDataFolder() + "/p","joinedPlayers" + ".yml");
        YamlConfiguration pData = new YamlConfiguration();
//        for (UUID uuid: joinedPlayersMap.keySet()){
//            String name = joinedPlayersMap.get(uuid);
//            pData.set(uuid.toString(),name);
//        }
        List<String> playerUUIDStrings = new ArrayList<>();
        List<String> playerNames = new ArrayList<>();
        for (UUID uuid: joinedPlayersMap.keySet()){
            String uuidString = uuid.toString();
            String playerName = joinedPlayersMap.get(uuid);
            playerUUIDStrings.add(uuidString);
            playerNames.add(playerName);
        }
        pData.set("playerUUIDStrings", playerUUIDStrings);
        pData.set("playerNames", playerNames);
        try{
            pData.save(pFile);
        }catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try{
            pFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
