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

package io.github.dediamondpro.xcatch.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import io.github.dediamondpro.xcatch.XCatch;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Utils {
    public static double getAngleDistance(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % 360;
        return phi > 180 ? 360 - phi : phi;
    }

    public static void banUser(Player player, Map<String, String> variables, String duration) {
        Date expires = null;
        long length = parseTime(duration);
        if (length != 0)
            expires = new Date(Instant.now().getEpochSecond() * 1000 + length);
        XCatch.INSTANCE.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(),
                replaceVariables(XCatch.config.getString("ban-reason"), variables), expires, "XCatch");
        player.kickPlayer(replaceVariables(XCatch.config.getString("ban-reason"), variables));
    }

    public static String capitalize(String string) {
        String[] split = string.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : split) {
            stringBuilder.append(str.substring(0, 1).toUpperCase()).append(str.substring(1)).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    public static ItemStack createItem(Material material, String displayName, String... lore) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        itemStack.setItemMeta(itemMeta);
        if (lore != null)
            itemMeta.setLore(Arrays.asList(lore));
        itemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createActionDataItem(Material material, String displayName, int actionDataIndex, String... lore) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        itemStack.setItemMeta(itemMeta);
        if (lore != null)
            itemMeta.setLore(Arrays.asList(lore));
        itemMeta.setDisplayName(displayName);
        itemMeta.getPersistentDataContainer().set(
                XCatch.INSTANCE.getActionDataKey(),
                PersistentDataType.INTEGER,
                actionDataIndex
        );
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static UUID getOfflineUUID(String name) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getUniqueId();
        }
        return null;
    }

    public static String getOfflineName(UUID uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getName();
        }
        return null;
    }

    public static ItemStack createSkull(ItemStack item, UUID id) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        item.setItemMeta(meta);

        return item;
    }

    public static String replaceVariables(String message, Map<String, String> variables) {
        for (String variable : variables.keySet()) {
            message = message.replace(variable, variables.get(variable));
        }
        return message;
    }

    private static TextChannel channel = null;

    public static void sendMessage(String message) {
        if (!XCatch.config.getString("discord-channel").trim().equals("") && XCatch.INSTANCE.getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
            if (channel == null) channel = DiscordUtil.getTextChannelById(XCatch.config.getString("discord-channel"));
            if (channel != null) DiscordUtil.sendMessage(channel, message);
            else XCatch.INSTANCE.logger.warning("Could not find the given discord channel!");
        }
    }

    public static JsonElement getRequest(String site) {
        try {
            URL url = new URL(site);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int status = con.getResponseCode();
            if (status != 200)
                return null;
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            JsonParser parser = new JsonParser();
            return parser.parse(content.toString());
        } catch (IOException ignored) {
        }
        return null;
    }

    public static void checkForUpdate() {
        CompletableFuture.runAsync(() -> {
            JsonElement data = getRequest("https://api.github.com/repos/dediamondpro/XCatch/releases");
            if (data == null) return;

            if (!data.getAsJsonArray().get(0).getAsJsonObject().get("tag_name").getAsString().equals(XCatch.INSTANCE.getDescription().getVersion())) {
                XCatch.INSTANCE.logger.warning("XCatch is out of date! Please download the latest version at: " +
                        data.getAsJsonArray().get(0).getAsJsonObject().get("html_url").getAsString());
            }
        });
    }

    public static long parseTime(String input) {
        long number = Long.parseLong(input.replaceAll("[^0-9]", ""));
        if (input.endsWith("y")) {
            return number * 31557600000L;
        } else if (input.endsWith("mo")) {
            return number * 2629800000L;
        } else if (input.endsWith("w")) {
            return number * 604800017L;
        } else if (input.endsWith("d")) {
            return number * 86400000L;
        } else if (input.endsWith("h")) {
            return number * 3600000L;
        } else if (input.endsWith("m")) {
            return number * 60000L;
        } else if (input.endsWith("s")) {
            return number * 1000L;
        }
        return number;
    }

    public static void broadcastTextComponent(TextComponent component, String permission) {
        for (Player player : XCatch.INSTANCE.getServer().getOnlinePlayers()) {
            if (!player.hasPermission(permission)) continue;
            player.spigot().sendMessage(component);
        }
    }
}
