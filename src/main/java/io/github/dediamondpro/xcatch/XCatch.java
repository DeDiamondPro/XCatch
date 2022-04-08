package io.github.dediamondpro.xcatch;

import io.github.dediamondpro.xcatch.commands.XCatchCommand;
import io.github.dediamondpro.xcatch.commands.XCatchTabCompleter;
import io.github.dediamondpro.xcatch.data.PersistentData;
import io.github.dediamondpro.xcatch.gui.ViewGui;
import io.github.dediamondpro.xcatch.listeners.OnBlockBreak;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class XCatch extends JavaPlugin {
    public static XCatch INSTANCE;
    public final Logger logger = getLogger();
    public static FileConfiguration config;
    public static final HashMap<Material, Integer> rareOres = new HashMap<>();
    public static Metrics metrics;
    public static int metricFlags = 0;
    public static int metricBans = 0;

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        config = getConfig();
        loadRareOres();

        if (new File(getDataFolder().getAbsolutePath() + "/data.json.gz").exists())
            PersistentData.loadData(getDataFolder().getAbsolutePath() + "/data.json.gz");

        getServer().getPluginManager().registerEvents(new OnBlockBreak(), this);
        getServer().getPluginManager().registerEvents(new ViewGui(), this);

        getCommand("xcatch").setExecutor(new XCatchCommand());
        getCommand("xcatch").setTabCompleter(new XCatchTabCompleter());
        getCommand("xcatch").setPermission("xcatch.command");
        getCommand("xcatch").setPermissionMessage("§8[§cXCatch§8] §cYou do not have permission to use that command");

        metrics = new Metrics(this, 14872);
        metrics.addCustomChart(new SingleLineChart("flags", () -> {
            int temp = metricFlags;
            metricFlags = 0;
            return temp;
        }));
        metrics.addCustomChart(new SingleLineChart("bans", () -> {
            int temp = metricBans;
            metricBans = 0;
            return temp;
        }));

        logger.log(Level.INFO, "XCatch has been initialized");
    }

    @Override
    public void onDisable() {
        PersistentData.saveData(getDataFolder().getAbsolutePath() + "/data.json.gz");
    }

    public static void loadRareOres() {
        ArrayList<HashMap<String, Integer>> list = (ArrayList<HashMap<String, Integer>>) config.get("rare-ores");
        if (list != null) {
            for (HashMap<String, Integer> map : list) {
                String ore = map.keySet().stream().findFirst().get();
                Material material = Material.getMaterial(ore.toUpperCase());
                if (material != null)
                    rareOres.put(material, map.get(ore));
            }
        }
        INSTANCE.logger.log(Level.INFO, "Rare ore list: " + rareOres);
    }
}
