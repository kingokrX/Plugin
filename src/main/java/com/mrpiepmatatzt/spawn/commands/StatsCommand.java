package com.mrpiepmatatzt.spawn.commands;

import com.mrpiepmatatzt.spawn.SpawnPlugin;
import com.mrpiepmatatzt.spawn.gui.ConfirmResetGUI;
import com.mrpiepmatatzt.spawn.gui.PlayerSelectGUI;
import com.mrpiepmatatzt.spawn.gui.ResetOptionsGUI;
import com.mrpiepmatatzt.spawn.gui.ResetOptionsGUI.ResetType;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class StatsCommand implements CommandExecutor, TabCompleter {

    private final SpawnPlugin plugin;

    public StatsCommand(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    private final List<String> MAIN_SUBCOMMANDS = Arrays.asList("reset", "new-season");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /stats <reset|new-season> [player]");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reset":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this command.");
                    return true;
                }
                Player player = (Player) sender;
                if (args.length == 1) {
                    // /stats reset -> open player select GUI
                    new PlayerSelectGUI(plugin, player).open();
                    return true;
                }
                if (args.length == 2) {
                    // /stats reset <player> -> open reset options GUI
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                        player.sendMessage("§cPlayer not found.");
                        return true;
                    }
                    new ResetOptionsGUI(plugin, player, target).open();
                    return true;
                }
                sender.sendMessage("§cUsage: /stats reset [player]");
                return true;

            case "new-season":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this command.");
                    return true;
                }
                Player p = (Player) sender;
                // Open confirmation GUI for new season reset
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    new ConfirmResetGUI(plugin, p, null, ResetType.NEW_SEASON).open();
                });
                return true;

            default:
                sender.sendMessage("§cUnknown subcommand. Use /stats <reset|new-season>");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Suggest subcommands
            return MAIN_SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            // Suggest online players for reset
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
