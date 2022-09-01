/*
 * This file is part of XCatch.
 *
 * XCatch is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * XCatch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see
 * <https://www.gnu.org/licenses/>.
 */

package io.github.dediamondpro.xcatch.listeners;

import io.github.dediamondpro.xcatch.XCatch;
import io.github.dediamondpro.xcatch.data.*;
import io.github.dediamondpro.xcatch.utils.FlagHandler;
import io.github.dediamondpro.xcatch.utils.Utils;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class OnBlockBreak implements Listener {
    public static final HashMap<UUID, HeadingData> data = new HashMap<>();
    private static final HashMap<UUID, ArrayList<PendingChangeData>> pendingChanges = new HashMap<>();
    public static final HashMap<UUID, HashMap<Material, Integer>> blocksMined = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().hasPermission("xcatch.bypass") || event.getClass() != BlockBreakEvent.class) return;
        Block blockBroken = event.getBlock();
        Location location = blockBroken.getLocation();
        if (location.getBlockY() > XCatch.config.getInt("max-height")) return;
        float dir = event.getPlayer().getEyeLocation().getYaw();
        if (dir < 0) {
            dir = 360 - Math.abs(dir);
        }
        UUID uuid = event.getPlayer().getUniqueId();

        if (XCatch.rareOres.containsKey(blockBroken.getBlockData().getMaterial()) && data.containsKey(uuid)) {
            if (!blocksMined.containsKey(uuid)) {
                HashMap<Material, Integer> temp = new HashMap<>();
                for (Material material : XCatch.rareOres.keySet()) {
                    temp.put(material, 0);
                }
                blocksMined.put(uuid, temp);
            }
            int amountFlag = XCatch.rareOres.get(blockBroken.getBlockData().getMaterial());
            int amountMined = blocksMined.get(uuid).get(blockBroken.getBlockData().getMaterial()) + 1;
            data.get(uuid).lastRareOre = Instant.now().getEpochSecond();
            blocksMined.get(uuid).put(blockBroken.getBlockData().getMaterial(), amountMined);
            if (data.get(uuid).changes >= XCatch.config.getInt("changes-for-flag") && amountMined >= amountFlag) {
                FlagHandler.addFlag(event, false);
            }
        } else if (!data.containsKey(uuid) || Instant.now().getEpochSecond() - data.get(uuid).lastRareOre > XCatch.config.getInt("grace-period")) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (XCatch.rareOres.containsKey(event.getPlayer().getWorld().getBlockAt(location.getBlockX() + x,
                                location.getBlockY() + y, location.getBlockZ() + z).getType())) {
                            return;
                        }
                    }
                }
            }
            if (!data.containsKey(uuid)) {
                pendingChanges.remove(uuid);
                HeadingData headingData = new HeadingData(dir);
                headingData.lastChange = Instant.now().getEpochSecond();
                data.put(uuid, headingData);
            } else if (Utils.getAngleDistance(dir, data.get(uuid).getAverage()) > XCatch.config.getInt("change-angle")) {
                boolean found = false;
                if (pendingChanges.containsKey(uuid)) {
                    for (PendingChangeData pendingChange : pendingChanges.get(uuid)) {
                        if (location.getX() == pendingChange.location.getX()
                                && location.getZ() == pendingChange.location.getZ() && (location.getY() - 1 == pendingChange.location.getY()
                                || location.getY() + 1 == pendingChange.location.getY())) {
                            HeadingData headingData = new HeadingData(dir, pendingChange.dir);
                            headingData.lastChange = Instant.now().getEpochSecond();
                            if (data.containsKey(uuid) &&
                                    headingData.lastChange - data.get(uuid).lastChange < XCatch.config.getInt("direction-change-retention")) {
                                headingData.changes = data.get(uuid).changes + 1;
                            }
                            data.put(uuid, headingData);
                            pendingChanges.remove(uuid);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    if (pendingChanges.containsKey(uuid) && pendingChanges.get(uuid).size() >= XCatch.config.getInt("max-pending"))
                        pendingChanges.get(uuid).remove(0);
                    if (!pendingChanges.containsKey(uuid))
                        pendingChanges.put(uuid, new ArrayList<>());
                    pendingChanges.get(uuid).add(new PendingChangeData(location, dir));
                }
            } else {
                pendingChanges.remove(uuid);
                if (Instant.now().getEpochSecond() - data.get(uuid).lastChange >= XCatch.config.getInt("direction-change-retention")) {
                    data.get(uuid).changes = 0;
                    blocksMined.remove(uuid);
                }
                data.get(uuid).headings.add(dir);
            }

            if (FlagHandler.flags.containsKey(uuid) && Instant.now().getEpochSecond() - FlagHandler.flags.get(uuid).lastFlag > XCatch.config.getInt("flag-retention") * 60L) {
                FlagHandler.flags.remove(uuid);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        data.remove(uuid);
        pendingChanges.remove(uuid);
        blocksMined.remove(uuid);
    }

    public static HashMap<UUID, HeadingData> getData() {
        return data;
    }

    public static HashMap<UUID, ArrayList<PendingChangeData>> getPendingChanges() {
        return pendingChanges;
    }

    public static HashMap<UUID, HashMap<Material, Integer>> getBlocksMined() {
        return blocksMined;
    }
}
