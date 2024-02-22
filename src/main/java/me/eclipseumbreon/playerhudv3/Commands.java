package me.eclipseumbreon.playerhudv3;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().matches("test")){
            if (sender instanceof ConsoleCommandSender){
                List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
                int online = onlinePlayers.size();
                if (online == 0)return false;
                Coordinate.addCurrentCoordinates(onlinePlayers.get(new Random().nextInt(0,online)), "non-standard", "test_coordinate");
            }else if (sender instanceof Player){
                Coordinate.addCurrentCoordinates(((Player)sender), "standard", "test_coordinate");
            }
        }
        return false;
    }
}
