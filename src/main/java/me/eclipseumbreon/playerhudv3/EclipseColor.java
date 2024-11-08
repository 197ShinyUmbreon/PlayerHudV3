package me.eclipseumbreon.playerhudv3;

public enum EclipseColor{
    RESET("§r"),
    YELLOW("§e"),
    RED("§c"),
    GOLD("§6"),
    AQUA("§b");



    public final String colorCode;
    EclipseColor(String colorCode){
        this.colorCode = colorCode;
    }
}
