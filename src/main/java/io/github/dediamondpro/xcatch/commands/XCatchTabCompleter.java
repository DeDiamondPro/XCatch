package io.github.dediamondpro.xcatch.commands;

import io.github.dediamondpro.xcatch.XCatch;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XCatchTabCompleter implements TabCompleter {
    private static final ArrayList<String> subCommands = new ArrayList<String>() {{
        add("help");
        add("view");
        add("reload");
        add("debug");
        add("info");
    }};

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equalsIgnoreCase("xcatch")) return null;
        if (args.length == 0) {
            return subCommands;
        } else if (args.length == 1) {
            ArrayList<String> answer = new ArrayList<>();
            for (String subCommand : subCommands) {
                if (args[0].toLowerCase().startsWith(args[0].toLowerCase()))
                    answer.add(subCommand);
            }
            return answer;
        } else if (args.length == 2) {
            switch (args[0]) {
                case "help":
                case "reload":
                case "info":
                    return Collections.emptyList();
                case "debug":
                case "view":
                    return getPlayerList(args[1]);
            }
        }
        return Collections.emptyList();
    }

    public static List<String> getPlayerList(String filter) {
        ArrayList<String> matchedPlayers = new ArrayList<>();
        for (Player player : XCatch.INSTANCE.getServer().getOnlinePlayers()) {
            String name = player.getName();
            if (StringUtil.startsWithIgnoreCase(name, filter)) {
                matchedPlayers.add(name);
            }
        }

        matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
        return matchedPlayers;
    }
}
