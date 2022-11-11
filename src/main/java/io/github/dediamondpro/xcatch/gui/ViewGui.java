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

package io.github.dediamondpro.xcatch.gui;

import io.github.dediamondpro.xcatch.XCatch;
import io.github.dediamondpro.xcatch.data.ActionData;
import io.github.dediamondpro.xcatch.data.PersistentData;
import io.github.dediamondpro.xcatch.data.PlayerData;
import io.github.dediamondpro.xcatch.utils.Utils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class ViewGui implements Listener {
    private static final SimpleDateFormat format = new SimpleDateFormat(XCatch.config.getString("date-format"));

    public static void openAllGui(Player p, int page) {
        Inventory gui = XCatch.INSTANCE.getServer().createInventory(p, 54, "§8[§cXCatch§8] All Players " + page + "/" +
                (int) Math.ceil(PersistentData.data.actions.size() / 45f));

        TreeMap<Long, PlayerData> sortedData = new TreeMap<>();
        for (int i = 45 * (page - 1); i < Math.min(45 * (page), PersistentData.data.actions.size()); i++) {
            UUID uuid = (UUID) PersistentData.data.actions.keySet().toArray()[i];
            long lastFlag = 0;
            int bans = 0;
            int flags = 0;
            for (ActionData data : PersistentData.data.actions.get(uuid)) {
                if (data.type == ActionData.ActionType.BAN) bans++;
                else flags++;
                if (data.time > lastFlag) lastFlag = data.time;
            }
            sortedData.put(lastFlag, new PlayerData(uuid, flags, bans));
        }
        int i = 0;
        for (long lastFlag : sortedData.descendingKeySet()) {
            PlayerData data = sortedData.get(lastFlag);
            String name = Utils.getOfflineName(data.uuid);
            if (name == null) name = data.uuid.toString();
            gui.setItem(i, Utils.createSkull(Utils.createItem(
                    Material.PLAYER_HEAD, "§e" + name,
                    "§7Last Flag: " + format.format(Date.from(Instant.ofEpochSecond(lastFlag))),
                    "§7All Time Flags: " + data.flags,
                    "§7All Time Bans: " + data.bans
            ), data.uuid));
            i++;
        }

        addBottomBar(gui);
        p.openInventory(gui);
    }

    public static void openPlayerGui(Player p, UUID uuid, String name, int page) {
        ArrayList<ActionData> sortedList = PersistentData.data.actions.get(uuid);
        Collections.sort(sortedList);
        Inventory gui = XCatch.INSTANCE.getServer().createInventory(p, 54, "§8[§cXCatch§8] " + name + " " + page + "/" +
                (int) Math.ceil(PersistentData.data.actions.get(uuid).size() / 45f));

        for (int i = 45 * (page - 1); i < Math.min(45 * (page), PersistentData.data.actions.get(uuid).size()); i++) {
            ActionData data = PersistentData.data.actions.get(uuid).get(i);

            World world = XCatch.INSTANCE.getServer().getWorld(data.worldUID);
            String worldName = world == null ? p.getWorld().getName() : world.getName();

            gui.setItem(i - 45 * (page - 1), Utils.createActionDataItem(
                    data.type == ActionData.ActionType.BAN ? Material.RED_CONCRETE : Material.YELLOW_CONCRETE,
                    data.type == ActionData.ActionType.BAN ? "§cBan" : "§eFlag",
                    i,
                    (data.ore != null ? "§7" + data.amount + " " + Utils.capitalize(data.ore) : "§7Unknown Ore"),
                    "§7" + worldName + " " + data.x + " " + data.y + " " + data.z + " (click to teleport)",
                    "§7" + format.format(Date.from(Instant.ofEpochSecond(data.time)))
            ));
        }

        addBottomBar(gui);
        p.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("§8[§cXCatch§8]")) {
            event.setCancelled(true);
            if (event.getView().getTitle().contains("All Players")) {
                int page = Integer.parseInt(event.getView().getTitle().split(" ")[3].split("/")[0]);
                switch (event.getSlot()) {
                    case 49:
                        event.getWhoClicked().closeInventory();
                        break;
                    case 48:
                        if (page > 1)
                            openAllGui((Player) event.getWhoClicked(), page - 1);
                        break;
                    case 50:
                        if (page < Math.ceil(PersistentData.data.actions.size() / 45f))
                            openAllGui((Player) event.getWhoClicked(), page + 1);
                        break;
                    default:
                        if (event.getSlot() < 45 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            String name = event.getCurrentItem().getItemMeta().getDisplayName().replaceAll("§[a-z0-9]", "");
                            UUID uuid = Utils.getOfflineUUID(name);
                            openPlayerGui((Player) event.getWhoClicked(), uuid, name, 1);
                        }
                }
            } else {
                int page = Integer.parseInt(event.getView().getTitle().split(" ")[2].split("/")[0]);
                String name = event.getView().getTitle().split(" ")[1];
                UUID uuid = Utils.getOfflineUUID(name);
                switch (event.getSlot()) {
                    case 49:
                        event.getWhoClicked().closeInventory();
                        break;
                    case 48:
                        if (page > 1)
                            openPlayerGui((Player) event.getWhoClicked(), uuid, name, page - 1);
                        break;
                    case 50:
                        if (page < Math.ceil(PersistentData.data.actions.get(uuid).size() / 45f))
                            openPlayerGui((Player) event.getWhoClicked(), uuid, name, page + 1);
                        break;
                    default:
                        if (event.getSlot() < 45 && event.getCurrentItem() != null && (event.getCurrentItem().getType() == Material.RED_CONCRETE
                                || event.getCurrentItem().getType() == Material.YELLOW_CONCRETE)) {
                            Integer actionIndex = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(
                                    XCatch.INSTANCE.getActionDataKey(), PersistentDataType.INTEGER
                            );
                            if (actionIndex == null) return;
                            ActionData actionData = PersistentData.data.actions.get(uuid).get(actionIndex);
                            try {
                                HashMap<String, String> variables = new HashMap<String, String>() {{
                                    put("{player}", name);
                                    put("{world}", XCatch.INSTANCE.getServer().getWorld(actionData.worldUID).getName());
                                    put("{x}", String.valueOf(actionData.x));
                                    put("{y}", String.valueOf(actionData.y));
                                    put("{z}", String.valueOf(actionData.z));
                                }};
                                event.getWhoClicked().closeInventory();
                                XCatch.INSTANCE.getServer().dispatchCommand(event.getWhoClicked(), Utils.replaceVariables(XCatch.config.getString("view-click-command"), variables));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                }
            }
        }
    }

    private static void addBottomBar(Inventory gui) {
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        gui.setItem(48, Utils.createItem(Material.ARROW, "§aLast Page"));
        gui.setItem(49, Utils.createItem(Material.BARRIER, "§cClose"));
        gui.setItem(50, Utils.createItem(Material.ARROW, "§aNext Page"));
    }
}
