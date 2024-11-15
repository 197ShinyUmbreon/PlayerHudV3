package me.eclipseumbreon.playerhudv3;

import me.eclipseumbreon.playerhudv3.playerchest.PlayerStorage;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;

public final class PlayerHudV3 extends JavaPlugin {

    public static final boolean debug = true;

    private static final String header = "[PlayerHudV3] ";
    private static final String headerColored = "§e" + header + "§r";

    public static PlayerHudV3 getPlugin(){return plugin;}
    private static PlayerHudV3 plugin;


    @Override
    public void onEnable() {
        plugin = this;
        //createCoordinateFileConfig();
        registerEvents();
        registerCommands();
        Players.initialize();
        Coordinate.initialize();
        PlayerStorage.initialize();
        Death.initialize();

        if (debug) debugRuntime();
    }

    private static void debugRuntime(){

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerEvents(){
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new Events(), this);
    }

    private void registerCommands(){
        Commands commands = new Commands();
        if (debug){
            PluginCommand test = this.getCommand("test");
            if (test != null) test.setExecutor(commands);
            PluginCommand test2 = this.getCommand("test2");
            if (test2 != null) test2.setExecutor(commands);
        }
        PluginCommand playerhud = this.getCommand("playerhud");
        if (playerhud != null){
            playerhud.setExecutor(commands);
            playerhud.setTabCompleter(commands);
        }
    }

    public static void cOut(String string){
        System.out.println(header + string);
    }

    public static void playerMessage(Player target, String message){
        target.sendMessage(headerColored + message);
    }

//    private File coordinateFile;
//    private FileConfiguration coordinateFileConfig;
//
//    public FileConfiguration getCoordinateFileConfig() {
//        return this.coordinateFileConfig;
//    }
//
//    private void createCoordinateFileConfig() {
//        coordinateFile = new File(this.getDataFolder(), "coordinates.yml");
//        if (!coordinateFile.exists()) {
//            if (!coordinateFile.getParentFile().mkdirs()){
//                System.out.println("Unable to create directory '" + coordinateFile.getParentFile().getPath() + "'");
//            }
//            this.saveResource("coordinates.yml", false);
//        }
//        coordinateFileConfig = new YamlConfiguration();
//        try {
//            coordinateFileConfig.load(coordinateFile);
//        } catch (IOException | InvalidConfigurationException ex) {
//            ex.printStackTrace();
//        }
//    }
//    public void saveCoordinateFile(){
//        try{
//            getCoordinateFileConfig().save(coordinateFile);
//        }catch (IOException ex){
//            ex.printStackTrace();
//        }
//    }
}
