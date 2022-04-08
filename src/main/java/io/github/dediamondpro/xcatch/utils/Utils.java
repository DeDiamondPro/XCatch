package io.github.dediamondpro.xcatch.utils;

import io.github.dediamondpro.xcatch.XCatch;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

public class Utils {
    public static double getAngleDistance(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % 360;
        return phi > 180 ? 360 - phi : phi;
    }

    public static void banUser(Player player) {
        Date expires = null;
        if (XCatch.config.getInt("ban-duration") != 0)
            expires = new Date(Instant.now().getEpochSecond() * 1000 + XCatch.config.getInt("ban-duration") * 86400000L);
        XCatch.INSTANCE.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), XCatch.config.getString("ban-reason"),
                expires, "XCatch");
        player.kickPlayer(XCatch.config.getString("ban-reason"));
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
}
