package com.mrpiepmatatzt.spawn.commands;
import com.mrpiepmatatzt.spawn.util.ColorUtil;

import com.mrpiepmatatzt.spawn.SpawnPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GivePickCommand implements CommandExecutor, TabCompleter {

    private final SpawnPlugin plugin;

    public GivePickCommand(SpawnPlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("GivePickCommand initialized with plugin instance: " + plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        plugin.getLogger().info("Executing givepick command. PickaxeManager is " + plugin.getPickaxeManager());

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§cUsage: /givepick <type>");
            return true;
        }

        String type = args[0].toLowerCase(); // Normalize type
        String permission = "kings." + type;

        if (!player.hasPermission(permission)) {
            player.sendMessage("§cYou do not have permission to receive this pickaxe (§7" + permission + "§c).");
            return true;
        }

        plugin.getPickaxeManager().givePickaxe(type, player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return plugin.getPickaxeManager().getPickaxeTypes().stream()
                    .filter(type -> type.startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
