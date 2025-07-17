package com.mrpiepmatatzt.spawn.listeners;

import com.mrpiepmatatzt.spawn.SpawnPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;

public class FirstJoinTeleportListener implements Listener {
    private final SpawnPlugin plugin;

    public FirstJoinTeleportListener(SpawnPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        if (!config.getBoolean("on-first-join-send-player-to-spawn", false)) {
            return; // Feature disabled
        }

        // Teleport if player has never joined or if their data was deleted
        if (!player.hasPlayedBefore() || isPlayerDataMissing(player)) {
            Location spawn = getSpawnLocationFromConfig(config);
            if (spawn != null) {
                player.teleport(spawn);
            } else {
                // Fallback to world's default spawn
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
        }
    }

    private boolean isPlayerDataMissing(Player player) {
        World world = Bukkit.getWorlds().get(0); // Main world
        File playerDataFile = new File(world.getWorldFolder(), "playerdata/" + player.getUniqueId() + ".dat");
        return !playerDataFile.exists();
    }

    private Location getSpawnLocationFromConfig(FileConfiguration config) {
        if (!config.contains("spawn")) {
            return null;
        }

        try {
            String worldName = config.getString("spawn.world");
            if (worldName == null) return null;

            World world = Bukkit.getWorld(worldName);
            if (world == null) return null;

            double x = config.getDouble("spawn.x");
            double y = config.getDouble("spawn.y");
            double z = config.getDouble("spawn.z");
            float yaw = (float) config.getDouble("spawn.yaw", 0);
            float pitch = (float) config.getDouble("spawn.pitch", 0);

            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load spawn location from config: " + e.getMessage());
            return null;
        }
    }
}
