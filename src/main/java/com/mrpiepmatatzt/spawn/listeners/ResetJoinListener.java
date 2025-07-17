package com.mrpiepmatatzt.spawn.listeners;

import com.mrpiepmatatzt.spawn.SpawnPlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.File;

public class ResetJoinListener implements Listener {

    private final SpawnPlugin plugin;

    public ResetJoinListener(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        File marker = new File(plugin.getDataFolder(), "reset-temp/" + player.getUniqueId());

        if (marker.exists()) {
            marker.delete();

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
                player.sendMessage(ChatColor.YELLOW + "You have been reset");
                player.sendMessage(ChatColor.BLUE + "Please Make a Ticket In The Discord.");
                player.sendMessage(ChatColor.RED + "By an Administrator");
            }, 5L);
        }
    }
}
