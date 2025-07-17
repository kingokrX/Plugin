package com.mrpiepmatatzt.spawn.commands;

import com.mrpiepmatatzt.spawn.SpawnPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final SpawnPlugin plugin;

    public ReloadCommand(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();        // Reload config.yml
            plugin.reloadItemsFile();    // Reload items.yml ✅
            sender.sendMessage("§aConfig and items.yml reloaded!");
        } else {
            sender.sendMessage("§cUsage: /kings reload");
        }
        return true;
    }
}
