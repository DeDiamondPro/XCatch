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
    private static final HashMap<UUID, HeadingData> data = new HashMap<>();
    private static final HashMap<UUID, FlagData> flags = new HashMap<>();
    private static final HashMap<UUID, ArrayList<PendingChangeData>> pendingChanges = new HashMap<>();
    private static final HashMap<UUID, HashMap<Material, Integer>> blocksMined = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().hasPermission("xcatch.bypass") || event.getClass() != BlockBreakEvent.class) return;
        Block blockBroken = event.getBlock();
        Location location = blockBroken.getLocation();
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
            String ore = blockBroken.getBlockData().getMaterial().toString().toLowerCase().replace("_", " ");
            data.get(uuid).lastRareOre = Instant.now().getEpochSecond();
            blocksMined.get(uuid).put(blockBroken.getBlockData().getMaterial(), amountMined);
            if (data.get(uuid).changes >= XCatch.config.getInt("changes-for-flag") && amountMined >= amountFlag) {
                if (flags.containsKey(uuid)) {
                    flags.get(uuid).flags++;
                    flags.get(uuid).lastFlag = Instant.now().getEpochSecond();
                } else {
                    flags.put(uuid, new FlagData(1, Instant.now().getEpochSecond()));
                }
                if (!PersistentData.data.actions.containsKey(uuid)) {
                    PersistentData.data.actions.put(uuid, new ArrayList<>());
                }
                HashMap<String, String> variables = new HashMap<String, String>() {{
                    put("{player}", event.getPlayer().getDisplayName());
                    put("{flags}", String.valueOf(flags.get(uuid).flags));
                    put("{ore}", ore);
                    put("{amount}", String.valueOf(amountMined));
                    put("{x}", String.valueOf(location.getBlockX()));
                    put("{y}", String.valueOf(location.getBlockY()));
                    put("{z}", String.valueOf(location.getBlockZ()));
                }};
                if (XCatch.config.getInt("ban-flags") != 0 && flags.get(uuid).flags >= XCatch.config.getInt("ban-flags")
                        && !event.getPlayer().hasPermission("xcatch.noban")) {
                    List<String> durations = XCatch.config.getStringList("ban-durations");
                    String duration = durations.get((int) Math.min(durations.size() - 1,
                            PersistentData.data.actions.get(uuid).stream().filter((actionData) -> actionData.type.equals(ActionData.ActionType.BAN)).count()));
                    variables.put("{duration}", duration.equals("0") ? "ever" : duration);
                    Utils.banUser(event.getPlayer(), variables, duration);
                    XCatch.INSTANCE.getServer().broadcast(Utils.replaceVariables(XCatch.config.getString("ban-message"), variables),
                            "xcatch.alert");
                    if (XCatch.config.getBoolean("message-ban"))
                        Utils.sendMessage(Utils.replaceVariables(XCatch.config.getString("ban-message-discord"), variables));
                    PersistentData.data.actions.get(uuid).add(new ActionData(ActionData.ActionType.BAN, Instant.now().getEpochSecond(), ore, amountMined,
                            location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                    PersistentData.data.totalBans++;
                } else if (XCatch.config.getInt("alert-flags") != 0 && flags.get(uuid).flags >= XCatch.config.getInt("alert-flags")) {
                    TextComponent component = new TextComponent(Utils.replaceVariables(XCatch.config.getString("alert-message"), variables));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + event.getPlayer().getName()));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§cClick to teleport.").create()));
                    Utils.broadcastTextComponent(component, "xcatch.alert");
                    if (XCatch.config.getBoolean("message-alert"))
                        Utils.sendMessage(Utils.replaceVariables(XCatch.config.getString("alert-message-discord"), variables));
                    PersistentData.data.actions.get(uuid).add(new ActionData(ActionData.ActionType.FLAG, Instant.now().getEpochSecond(), ore, amountMined,
                            location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                } else // no ban and no alert but still a flag
                    PersistentData.data.actions.get(uuid).add(new ActionData(ActionData.ActionType.FLAG, Instant.now().getEpochSecond(), ore, amountMined,
                            location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                if (XCatch.commands.containsKey(flags.get(uuid).flags)) {
                    ArrayList<String> commands = XCatch.commands.get(flags.get(uuid).flags);
                    for (String command : commands) {
                        XCatch.INSTANCE.getServer().dispatchCommand(XCatch.INSTANCE.getServer().getConsoleSender(),
                                Utils.replaceVariables(command, variables));
                    }
                }
                PersistentData.data.totalFlags++;
                XCatch.metricFlags++;
                data.remove(uuid);
                blocksMined.remove(uuid);
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

            if (flags.containsKey(uuid) && Instant.now().getEpochSecond() - flags.get(uuid).lastFlag > XCatch.config.getInt("flag-retention") * 60L) {
                flags.remove(uuid);
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

    public static HashMap<UUID, FlagData> getFlags() {
        return flags;
    }

    public static HashMap<UUID, ArrayList<PendingChangeData>> getPendingChanges() {
        return pendingChanges;
    }

    public static HashMap<UUID, HashMap<Material, Integer>> getBlocksMined() {
        return blocksMined;
    }
}
