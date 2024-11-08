if (args.size() < 3){
        playerMessage(player, csHeader + ChatColor.RED + "Invalid coordinates." + ChatColor.YELLOW + "/ph cs add xx yy zz (optional) world (optional) name");
        return;
        }
        int x;
        int y;
        int z;
        try{
        x = Integer.parseInt(args.get(0));
        y = Integer.parseInt(args.get(1));
        z = Integer.parseInt(args.get(2));
        }catch (NumberFormatException e){
        playerMessage(player, csHeader + ChatColor.RED + "Invalid coordinates." + ChatColor.YELLOW + "/ph cs add xx yy zz (optional) world (optional) name");
        return;
        }
        World world;
        if (args.size() >= 4){
        world = Bukkit.getWorld(args.get(3));
        }else world = player.getWorld();
        if (world == null){
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (World bukkitWorld:Bukkit.getWorlds()){
        if (i != 0)stringBuilder.append(ChatColor.YELLOW).append(", ").append(ChatColor.RESET);
        stringBuilder.append(bukkitWorld.getName());
        i++;
        }
        stringBuilder.append(ChatColor.YELLOW).append(".");
        playerMessage(player, csHeader + ChatColor.RED + "Invalid world name." +
        ChatColor.YELLOW + " Worlds: " + ChatColor.RESET + stringBuilder.toString()
        );
        return;
        }
        String name;
        if (args.size() >= 5){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 4; i < args.size(); i++) {
        if (i != 4)stringBuilder.append(" ");
        stringBuilder.append(args.get(i));
        }
        name = stringBuilder.toString();
        }else name = "Saved Coordinate";
        Location location = new Location(world, x, y, z);

        // Old file handling
private static void removeAllPlayerCoordinatesFromFile(Player player){
        String stringUUID = player.getUniqueId().toString();
        file.set(stringUUID, null);
        plugin.saveCoordinateFile();
        }
private static void removeCoordinateFromFile(Coordinate coordinate){
        file.set(coordinate.owner + "." + coordinate.context + "." + coordinate.number, null);
        plugin.saveCoordinateFile();
        }
private static List<Coordinate> getPlayerCoordinatesFromFile(Player player){
        String stringUUID = player.getUniqueId().toString();
        List<Coordinate> coordinates = new ArrayList<>();
        if (file.get(stringUUID) == null){
        if (debug) System.out.println(player.getName() + "'s UUID does not exist in file. " + stringUUID); //debug
        return coordinates;
        }
        for (String context:contextTypes){
        String home = stringUUID + "." + context;
        int amount = file.getInt(home + ".amount");
        if (amount == 0){
        if (debug) System.out.println(player.getName() + " with context '" + context + "' had zero listings."); //debug
        continue;
        }
        for (int i = 1; i <= amount; i++) {
        String parsedName = file.getString(home + "." + i + "." + "name");
        if (parsedName == null){
        if (debug) System.out.println(player.getName() + " with context '" + context + "' under listing " + i + " name returned null"); //debug
        continue;
        }
        Location parsedLocation = file.getLocation(home + "." + i + "." + "location");
        if (parsedLocation == null){
        if (debug) System.out.println(player.getName() + " with context '" + context + "' under listing " + i + " location returned null"); //debug
        continue;
        }
        ItemStack parsedIcon = file.getItemStack(home + "." + i + "." + "icon");
        if (parsedIcon == null){
        if (debug) System.out.println(player.getName() + " with context '" + context + "' under listing " + i + " icon returned null"); //debug
        continue;
        }
        coordinates.add(new Coordinate(player.getUniqueId(),i,parsedName,context,parsedLocation,parsedIcon));
        }
        }
        return coordinates;
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

//        DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern("uMgHmsv").toFormatter();
        //DateTimeFormatterBuilder dtfb = new DateTimeFormatterBuilder().appendPattern("yyyy/MM/dd/");
//        DateTimeFormatterBuilder dtfb = new DateTimeFormatterBuilder()
//                .appendValue(ChronoField.YEAR)
//                .appendValue(ChronoField.MONTH_OF_YEAR)
//                .appendValue(ChronoField.DAY_OF_MONTH)
//                .appendValue(ChronoField.HOUR_OF_DAY)
//                .appendValue(ChronoField.MINUTE_OF_HOUR)
//                .appendValue(ChronoField.SECOND_OF_MINUTE)
//        ;
        //DateTimeFormatterBuilder formatDateBuilder = new DateTimeFormatterBuilder().appendPattern("yyyyMMdd");
        DateTimeFormatterBuilder formatDateBuilder = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR)
        .appendValue(ChronoField.MONTH_OF_YEAR)
        .appendValue(ChronoField.DAY_OF_MONTH)
        ;
        //DateTimeFormatterBuilder formatTimeBuilder = new DateTimeFormatterBuilder().appendPattern("kk:HH:mm:ss.SSS");
        DateTimeFormatterBuilder formatTimeBuilder = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.HOUR_OF_DAY)
        .appendValue(ChronoField.MINUTE_OF_HOUR)
        .appendValue(ChronoField.SECOND_OF_MINUTE)
        ;
        //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        DateTimeFormatter formatDate = formatDateBuilder.toFormatter();
        DateTimeFormatter formatTime = formatTimeBuilder.toFormatter();
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        String textDate = date.format(formatDate);
        String textTime = time.format(formatTime);
        LocalDate parsedDate = LocalDate.parse(textDate, formatDate);
        LocalTime parsedTime = LocalTime.parse(textTime, formatTime);
        System.out.println("Date String: " + textDate);
        System.out.println("Time String: " + textTime);



