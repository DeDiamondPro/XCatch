package io.github.dediamondpro.xcatch.data;

import org.bukkit.Location;

public class PendingChangeData {
    public Location location;
    public float dir;

    public PendingChangeData(Location location, float dir) {
        this.location = location;
        this.dir = dir;
    }
}
