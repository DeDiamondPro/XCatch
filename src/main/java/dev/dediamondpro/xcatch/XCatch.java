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

package dev.dediamondpro.xcatch;

import dev.dediamondpro.xcatch.commands.XCatchCommand;
import dev.dediamondpro.xcatch.commands.XCatchTabCompleter;
import dev.dediamondpro.xcatch.data.PersistentData;
import dev.dediamondpro.xcatch.listeners.OnBlockBreak;
import dev.dediamondpro.xcatch.utils.Utils;
import dev.dediamondpro.xcatch.gui.ViewGui;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public final class XCatch extends JavaPlugin {
    public static XCatch INSTANCE;
    public final Logger logger = getLogger();
    public static FileConfiguration config;
    public static final HashMap<Material, Integer> rareOres = new HashMap<>();
    public static final HashMap<Integer, ArrayList<String>> commands = new HashMap<>();
    public static int metricFlags = 0;

    private NamespacedKey actionDataKey;

    @Override
    public void onEnable() {
        INSTANCE = this;
        actionDataKey = new NamespacedKey(this, "action_data");

        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        } else {
            // System to update config
            FileConfiguration newConfig = (FileConfiguration) getConfig().getDefaults();
            boolean changed = false;
            for (String option : newConfig.getKeys(true)) {
                if (getConfig().contains(option, true))
                    newConfig.set(option, getConfig().get(option));
                else
                    changed = true;
            }
            if (changed) {
                try {
                    newConfig.save(new File(getDataFolder(), "config.yml"));
                    reloadConfig();
                } catch (IOException ignored) {
                }
            }
        }
        config = getConfig();
        loadConfigParts();

        if (config.getBoolean("check-update"))
            Utils.checkForUpdate();

        if (new File(getDataFolder().getAbsolutePath() + "/data.json.gz").exists())
            PersistentData.loadData(getDataFolder().getAbsolutePath() + "/data.json.gz");

        getServer().getPluginManager().registerEvents(new OnBlockBreak(), this);
        getServer().getPluginManager().registerEvents(new ViewGui(), this);

        getCommand("xcatch").setExecutor(new XCatchCommand());
        getCommand("xcatch").setTabCompleter(new XCatchTabCompleter());
        getCommand("xcatch").setPermission("xcatch.command");
        getCommand("xcatch").setPermissionMessage("§8[§cXCatch§8] §cYou do not have permission to use that command");

        // bstats metrics, can be disabled in global bstats config
        Metrics metrics = new Metrics(this, 14872);
        metrics.addCustomChart(new SingleLineChart("flags", () -> {
            int temp = metricFlags;
            metricFlags = 0;
            return temp;
        }));

        logger.info("XCatch has been initialized");
    }

    @Override
    public void onDisable() {
        PersistentData.saveData(getDataFolder().getAbsolutePath() + "/data.json.gz");
    }

    public NamespacedKey getActionDataKey() {
        return actionDataKey;
    }

    public static void loadConfigParts() {
        rareOres.clear();
        ArrayList<HashMap<String, Integer>> list = (ArrayList<HashMap<String, Integer>>) config.get("rare-ores");
        if (list != null) {
            for (HashMap<String, Integer> map : list) {
                String ore = map.keySet().stream().findFirst().get();
                Material material = Material.getMaterial(ore.toUpperCase());
                if (material != null)
                    rareOres.put(material, map.get(ore));
            }
        }

        commands.clear();
        ArrayList<HashMap<String, ArrayList<String>>> commandData = (ArrayList<HashMap<String, ArrayList<String>>>) config.get("commands");
        if (commandData != null) {
            for (HashMap<String, ArrayList<String>> map : commandData) {
                String flagCount = map.keySet().stream().findFirst().get();
                if (flagCount.contains("-")) {
                    String[] split = flagCount.split("-");
                    if (split.length != 2) {
                        return;
                    }
                    int startNum = Integer.parseInt(split[0].replaceAll("[^0-9]", ""));
                    int endNum = Integer.parseInt(split[1].replaceAll("[^0-9]", ""));
                    for (int i = startNum; i <= endNum; i++) {
                        if (!commands.containsKey(i))
                            commands.put(i, map.get(flagCount));
                        else {
                            ArrayList<String> newArray = new ArrayList<>(commands.get(i));
                            newArray.addAll(map.get(flagCount));
                            commands.put(i, newArray);
                        }
                    }
                } else {
                    int flags = Integer.parseInt(flagCount.replaceAll("[^0-9]", ""));
                    if (!commands.containsKey(flags))
                        commands.put(flags, map.get(flagCount));
                    else
                        commands.get(flags).addAll(map.get(flagCount));
                }
            }
        }
    }
}
