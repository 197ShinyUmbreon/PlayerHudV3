package me.eclipseumbreon.playerhudv3;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class PlayerHudV3 extends JavaPlugin {

    public static PlayerHudV3 getPlugin(){return plugin;}
    private static PlayerHudV3 plugin;

    @Override
    public void onEnable() {
        plugin = this;
        registerCommands();
        createCoordinateFileConfig();
    }

    private void registerCommands(){
        PluginCommand test = this.getCommand("test");
        if (test != null) test.setExecutor(new Commands());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private File coordinateFile;
    private FileConfiguration coordinateFileConfig;

    public FileConfiguration getCoordinateFileConfig() {
        return this.coordinateFileConfig;
    }

    private void createCoordinateFileConfig() {
        coordinateFile = new File(this.getDataFolder(), "coordinates.yml");
        if (!coordinateFile.exists()) {
            coordinateFile.getParentFile().mkdirs();
            this.saveResource("coordinates.yml", false);
        }
        coordinateFileConfig = new YamlConfiguration();
        try {
            coordinateFileConfig.load(coordinateFile);
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
    }
    public void saveCoordinateFile(){
        try{
            getCoordinateFileConfig().save(coordinateFile);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
