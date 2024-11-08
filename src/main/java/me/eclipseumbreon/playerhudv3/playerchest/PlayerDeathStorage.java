package me.eclipseumbreon.playerhudv3.playerchest;

import me.eclipseumbreon.playerhudv3.Death;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class PlayerDeathStorage extends PlayerStorage {

    public PlayerDeathStorage(UUID ownerID, UUID containerID, String title, Location location, boolean isLarge, List<ItemStack> contents) {
        super(ownerID, containerID, title, location, isLarge, contents);
    }

    private Death death;

    public Death getDeath() {
        return death;
    }

    public void setDeath(Death death) {
        this.death = death;
    }
}
