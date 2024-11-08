package me.eclipseumbreon.playerhudv3;

import me.eclipseumbreon.playerhudv3.playerchest.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class Commands implements CommandExecutor, TabCompleter {

    private static final boolean debug = PlayerHudV3.debug;

    // Command Capture -------------------------------------------------------------------------------------------------
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (debug){
            if (command.getName().matches("test")){
                if (sender instanceof ConsoleCommandSender){
                    return true;
                }else if (sender instanceof Player){
                    Player player = (Player) sender;
                    //open chest
                    PlayerStorage.testOpenRandomStorage(player);
                    return true;
                }
            }else if (command.getName().matches("test2")){
                if (sender instanceof ConsoleCommandSender){
                    return true;
                }else if (sender instanceof Player){
                    Player player = (Player) sender;

                    return true;
                }
            }
        }
        if (command.getName().matches("playerhud")){
            if (sender instanceof ConsoleCommandSender){
                return true;
            }else if (sender instanceof Player){
                Player player = (Player) sender;
                int arguments = args.length;
                if (arguments == 0){
                    if (debug)sendMessage(player, "This would normally open the menu...");
                }else{
                    argsHandler(player, args);
                }
                return true;
            }
        }
        return false;
    }

    private static void sendMessage(CommandSender recipient, String message){
        if (recipient instanceof Player){
            PlayerHudV3.playerMessage((Player) recipient, message);
        }else if (recipient instanceof ConsoleCommandSender){
            PlayerHudV3.cOut(message);
        }
    }

    // Tab Completer ---------------------------------------------------------------------------------------------------
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> commands = new ArrayList<>();
        List<String> completions = new ArrayList<>();
        if (!(sender instanceof Player))return commands;
        Player player = (Player) sender;
        int size = args.length;
        if (command.getName().matches("playerhud")){
            if (size == 1){
                String zero = args[0];
                commands.addAll(mainFunctions);
                StringUtil.copyPartialMatches(zero, commands, completions);
            }else if (size == 2){
                String zero = args[0];
                String one = args[1];
                if (zero.matches("cs")){
                    commands.addAll(csFunctions);
                }else if (zero.matches("ps")){
                    commands.addAll(psFunctions);
                }
                StringUtil.copyPartialMatches(one, commands, completions);
            }else if (size == 3){
                String zero = args[0];
                String one = args[1];
                String two = args[2];
                if (zero.matches("cs")){
                    if (one.matches("tp")){
                        commands.addAll(Coordinate.getContextualPlayerCoordinateNames(player, "saved"));
                    }else if (one.matches("add")){
                        commands.add("[optional] <Name>"); // better way to show context?
                    }else if (one.matches("list")){
                        // empty
                    }else if (one.matches("info")){
                        commands.addAll(Coordinate.getContextualPlayerCoordinateNames(player, "saved"));
                    }else if (one.matches("remove")){
                        commands.addAll(Coordinate.getContextualPlayerCoordinateNames(player, "saved"));
                    }else if (one.matches("rename")){
                        commands.addAll(Coordinate.getContextualPlayerCoordinateNames(player, "saved"));
                    }else if (one.matches("share")){
                        List<String> players = new ArrayList<>();
                        for (Player online:Bukkit.getOnlinePlayers()){
                            if (online.equals(player))continue;
                            players.add(online.getName());
                        }
                        if (players.size() == 0) players.add("[No Other Players Online]");
                        commands.addAll(players);
                    }
                }else if (zero.matches("ps")){
                    if (one.matches("list") || one.matches("open")){
                        if (player.isOp()){
                            commands.addAll(Players.getJoinedPlayerNames());
                        }else{
                            commands.add(player.getName());
                        }
                    }
                }
                StringUtil.copyPartialMatches(two, commands, completions);
            }else if (size == 4){
                String zero = args[0];
                String one = args[1];
                String two = args[2];
                String three = args[3];
                if (zero.matches("cs")){
                    if (one.matches("share")){
                        commands.addAll(Coordinate.getContextualPlayerCoordinateNames(player, "saved"));
                    }else if (one.matches("rename")){
                        commands.add("<New Name>"); // better way to show context?
                    }
                }else if (zero.matches("ps")){
                    if (one.matches("open")){
                        commands.addAll(PlayerStorage.listOfContainerIDs(player.getUniqueId()));
                    }else if (one.matches("list")){
                        // empty
                    }
                }
                StringUtil.copyPartialMatches(two, commands, completions);
            }
        }
        Collections.sort(completions);
        return commands;
    }

    private static void listOutFunctions(Player player, List<String> functions, String header){
        String sep = ", ";
        StringBuilder stringBuilder = new StringBuilder();
        if (header != null) stringBuilder.append(header);
        int i = 0;
        for (String function:functions){
            if (i != 0) stringBuilder.append(sep);
            stringBuilder.append(function);
            i++;
        }
        stringBuilder.append(".");
        sendMessage(player, stringBuilder.toString());
    }

    /*
    "cs" = Coordinate Storage
    "ps" = Player Storage
     */
    private static final List<String> mainFunctions = List.of("cs", "ps");
    private static void argsHandler(Player player, String[] args){
        if (debug) System.out.println(player.getName() + " sent '/playerhud '" + Arrays.toString(args) + "'");
        String func = args[0];
        if (!mainFunctions.contains(func) && !func.matches("help")){
            sendMessage(player, ChatColor.RED + "Invalid function.");
        }
        List<String> argsList = new ArrayList<>(List.of(args));
        argsList.remove(0);
        if (func.matches("help")){
            listOutFunctions(player, mainFunctions, null);
            return;
        }
        if (func.matches("cs")){
            coordinateStorage(player, argsList);
        }else if (func.matches("ps")){
            playerStorage(player, argsList);
        }
    }
    // Player Storage --------------------------------------------------------------------------------------------------
    private static final List<String> psFunctions = List.of("list", "open");
    private static final String psHeader = ChatColor.GOLD + "Player Storage:" + ChatColor.RESET + " ";
    private static void playerStorage(Player player, List<String> args){
        if (args.size() == 0){
            sendMessage(player, psHeader + ChatColor.RED + "No arguments..");
            return;
        }
        String func = args.get(0);
        args.remove(0);
        if (func.matches("help")){
            listOutFunctions(player, psFunctions, psHeader);
        }else if (func.matches("list")){
            psList(player, args);
        }else if (func.matches("open")){
            psOpen(player, args);
        }
    }
    private static void psList(Player player, List<String> args){
        String targetPlayerString = args.get(0);
        System.out.println(targetPlayerString);
        UUID targetPlayerUUID = Players.getPlayerUUIDFromName(targetPlayerString);
        if (targetPlayerUUID == null){
            sendMessage(
                player, psHeader +
                    ChatColor.RED + "Could not find Player of name '" +
                    ChatColor.AQUA + targetPlayerString +
                    ChatColor.RED + "'..."
            );
            return;
        }

        List<String> containerIDStrings = PlayerStorage.listOfContainerIDs(targetPlayerUUID);
        if (containerIDStrings.size() == 0){
            sendMessage(player, psHeader + ChatColor.RED + "No saved Storages..");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        //int i = 0;
        for (String name:containerIDStrings){
            stringBuilder.append(ChatColor.YELLOW).append("[").append(ChatColor.RESET);
            stringBuilder.append(name);
            stringBuilder.append(ChatColor.YELLOW).append("]").append(ChatColor.RESET).append("\n");
            //i++;
        }
        //stringBuilder.append(".");
        sendMessage(player, psHeader + ChatColor.YELLOW + "Saved Storages:\n" + ChatColor.RESET + stringBuilder.toString());
    }

    private static void psOpen(Player player, List<String> args){
        if (args.size() == 0){
            sendMessage(player, psHeader + ChatColor.RED + "No Player name given..");
        }else if (args.size() == 1){
            sendMessage(player, psHeader + ChatColor.RED + "No Storage ID given..");
        }else if (args.size() > 2){
            sendMessage(player, psHeader + ChatColor.RED + "Too many arguments..");
            return;
        }
        String targetPlayerString = args.get(0);
        System.out.println(targetPlayerString);
        UUID targetPlayerUUID = Players.getPlayerUUIDFromName(targetPlayerString);
        if (targetPlayerUUID == null){
            sendMessage(
                player, psHeader +
                    ChatColor.RED + "Could not find Player of name '" +
                    ChatColor.AQUA + targetPlayerString +
                    ChatColor.RED + "'..."
            );
            return;
        }
        List<String> containerIDStrings = PlayerStorage.listOfContainerIDs(targetPlayerUUID);
        String parsedIDString = args.get(1);
        for (String containerIDString:containerIDStrings){
            if (parsedIDString.matches(containerIDString)){
                PlayerStorage.openTargetPlayerStorage(player, UUID.fromString(containerIDString));
                sendMessage(player, psHeader + ChatColor.YELLOW + "Opened Storage: " + ChatColor.AQUA + containerIDString);
                break;
            }
        }

    }

    // Coordinate Storage -----------------------------------------------------------------------------------------------
    private static final List<String> csFunctions = List.of("tp", "add", "list", "info", "remove", "rename", "share");
    private static final String csHeader = ChatColor.GOLD + "Coordinate Storage:" + ChatColor.RESET + " ";
    private static void coordinateStorage(Player player, List<String> args){
        if (args.size() == 0){
            sendMessage(player, csHeader + ChatColor.RED + "No arguments..");
            return;
        }
        String func = args.get(0);
        args.remove(0);
        if (func.matches("help")){
            listOutFunctions(player, csFunctions, csHeader);
        }else if (func.matches("tp")){
            csTp(player, args);
        }else if (func.matches("add")){
            csAdd(player, args);
        }else if (func.matches("list")){
            csList(player);
        }else if (func.matches("info")){
            csInfo(player, args);
        }else if (func.matches("remove")){
            csRemove(player, args);
        }else if (func.matches("rename")){
            csRename(player, args);
        }else if (func.matches("share")){
            csShare(player, args);
        }
    }
    private static void csTp(Player player, List<String> args){
        if (args.size() == 0){
            sendMessage(player, csHeader + ChatColor.RED + "No destination given..");
            return;
        }
        Coordinate coordinate = getCoordinateByArgs(player, "saved", args);
        if (coordinate == null){
            sendMessage(player, csHeader + ChatColor.RED + "No matching coordinate found..");
            return;
        }
        sendMessage(player, csHeader + ChatColor.YELLOW + "Teleporting to saved coordinate: " +
                ChatColor.AQUA + coordinate.getName() + ChatColor.YELLOW + "."
        );
        player.teleport(coordinate.getLocation().add(0.5,0.2,0.5));
    }
    private static void csAdd(Player player, List<String> args){
        String name;
        if (args.size() == 0){
            name = "Saved Coordinate";
        }else{
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < args.size(); i++) {
                if (i != 0)stringBuilder.append(" ");
                stringBuilder.append(args.get(i));
            }
            name = stringBuilder.toString();
        }
        if (Coordinate.addCurrentCoordinates(player, "saved", name)){
            sendMessage(player, csHeader + ChatColor.YELLOW + "Saved coordinate! \"" +
                    ChatColor.AQUA + name + ChatColor.YELLOW + "\"."
            );
        }else{
            sendMessage(player, csHeader + ChatColor.RED + "Maximum saved coordinates. Cannot save new coordinate.");
        }
    }
    private static void csList(Player player){
        List<String> names = new ArrayList<>();
        for (Coordinate coordinate:Coordinate.getContextualPlayerCoordinates(player, "saved")){
            names.add(coordinate.getName());
        }
        if (names.size() == 0){
            sendMessage(player, csHeader + ChatColor.RED + "No saved coordinates..");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (String name:names){
            if (i != 0) stringBuilder.append(ChatColor.YELLOW).append(", ").append(ChatColor.RESET);
            stringBuilder.append(name);
            i++;
        }
        stringBuilder.append(".");
        sendMessage(player, csHeader + ChatColor.YELLOW + "Saved Coordinates:\n" + ChatColor.RESET + stringBuilder.toString());
    }
    private static void csInfo(Player player, List<String> args){
        if (args.size() == 0){
            sendMessage(player, csHeader + ChatColor.RED + "No coordinate name given..");
            return;
        }

        Coordinate coordinate = getCoordinateByArgs(player, "saved", args);
        if (coordinate == null){
            sendMessage(player, csHeader + ChatColor.RED + "No matching coordinate found..");
            return;
        }
        Location location = coordinate.getLocation();
        String worldName;
        if (location.getWorld() != null){
            worldName = location.getWorld().getName();
        }else worldName = "Unknown";
        sendMessage(player, csHeader + ChatColor.YELLOW + "Coordinate Info:\n" +
                ChatColor.YELLOW + "Name: " + ChatColor.AQUA + coordinate.getName() +
                ChatColor.YELLOW + "\nWorld: " + ChatColor.AQUA + worldName +
                ChatColor.YELLOW + " X: " + ChatColor.AQUA + location.getBlockX() +
                ChatColor.YELLOW + " Y: " + ChatColor.AQUA + location.getBlockY() +
                ChatColor.YELLOW + " Z: " + ChatColor.AQUA + location.getBlockZ()
        );
    }
    private static void csRemove(Player player, List<String> args){
        if (args.size() == 0){
            sendMessage(player, csHeader + ChatColor.RED + "No coordinate name given..");
            return;
        }
        Coordinate coordinate = getCoordinateByArgs(player, "saved", args);
        if (coordinate == null){
            sendMessage(player, csHeader + ChatColor.RED + "No matching coordinate found..");
            return;
        }
        sendMessage(player, csHeader + ChatColor.YELLOW + "Coordinate removed! \"" +
                ChatColor.AQUA + coordinate.getName() + ChatColor.YELLOW + "\"."
        );
        Coordinate.removeCoordinate(player, coordinate);
    }
    private static void csRename(Player player, List<String> args){
        if (args.size() == 0){
            sendMessage(player, csHeader + ChatColor.RED + "No coordinate name given..");
            return;
        }
        Coordinate coordinate = getCoordinateByArgs(player, "saved", args);
        if (coordinate == null){
            sendMessage(player, csHeader + ChatColor.RED + "No matching coordinate found..");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (String arg:args){
            if (i != 0) stringBuilder.append(" ");
            stringBuilder.append(arg);
            i++;
        }
        stringBuilder.replace(0,coordinate.getName().length() + 1, "");
        if (stringBuilder.toString().length() == 0){
            sendMessage(player, csHeader + ChatColor.RED + "New name cannot be empty.");
            return;
        }
        String newName = stringBuilder.toString();
        Coordinate.removeCoordinate(player, coordinate);
        sendMessage(player, csHeader + ChatColor.YELLOW + "Coordinate \"" +
                ChatColor.AQUA + coordinate.getName() + ChatColor.YELLOW + "\" renamed to \"" +
                ChatColor.AQUA + newName + ChatColor.YELLOW + "\"."
        );
        coordinate.setName(newName);
        Coordinate.addCoordinate(coordinate);
    }
    private static void csShare(Player player, List<String> args){
        if (args.size() == 0){
            sendMessage(player, csHeader + ChatColor.RED + "No recipient selected..");
            return;
        }else if (args.size() == 1){
            sendMessage(player, csHeader + ChatColor.RED + "No coordinate selected to share..");
            return;
        }
        String targetName = args.get(0);
        Player target = Bukkit.getPlayer(targetName);
        if (target == null){
            sendMessage(player, csHeader + ChatColor.RED + "Player is offline or name is invalid..");
            return;
        }
        args.remove(0);
        Coordinate coordinate = getCoordinateByArgs(player, "saved", args);
        if (coordinate == null){
            sendMessage(player, csHeader + ChatColor.RED + "No matching coordinate found..");
            return;
        }
        coordinate.setOwner(target.getUniqueId());
        Coordinate.addCoordinate(coordinate);
        sendMessage(player, csHeader + ChatColor.YELLOW + "Sent coordinate \"" +
                ChatColor.AQUA + coordinate.getName() + ChatColor.YELLOW + "\" to " +
                ChatColor.AQUA + target.getDisplayName() + ChatColor.RESET + ChatColor.YELLOW + "!"
        );
        sendMessage(target, csHeader + ChatColor.YELLOW + "Received the coordinate \"" +
                ChatColor.AQUA + coordinate.getName() + ChatColor.YELLOW + "\" from " +
                ChatColor.AQUA + player.getDisplayName() + ChatColor.RESET + ChatColor.YELLOW + "."
        );
    }
    // Coordinate Storage ----- Helper Functions ---------------------------------------
    private static Coordinate getCoordinateByArgs(Player player, String context, List<String> args){
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (String string:args){
            if (i != 0)stringBuilder.append(" ");
            stringBuilder.append(string);
            Coordinate coordinate = Coordinate.getCoordinateByName(player, context, stringBuilder.toString());
            if (coordinate != null) return coordinate;
            i++;
        }
        return null;
    }
}
