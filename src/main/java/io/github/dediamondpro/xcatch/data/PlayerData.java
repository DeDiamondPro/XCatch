package io.github.dediamondpro.xcatch.data;

import java.util.UUID;

public class PlayerData {
    public UUID uuid;
    public int flags;
    public int bans;

    public PlayerData(UUID uuid, int flags, int bans) {
        this.uuid = uuid;
        this.flags = flags;
        this.bans = bans;
    }
}
