

package com.mrpiepmatatzt.spawn;

import nl.marido.deluxecombat.api.DeluxeCombatAPI;


import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.mrpiepmatatzt.spawn.commands.GivePickCommand;
import com.mrpiepmatatzt.spawn.commands.ReloadCommand;
import com.mrpiepmatatzt.spawn.items.PickaxeManager;
import com.mrpiepmatatzt.spawn.commands.StatsCommand;
import com.mrpiepmatatzt.spawn.listeners.FirstJoinTeleportListener;
import com.mrpiepmatatzt.spawn.listeners.MultiBreakListener;
import com.mrpiepmatatzt.spawn.listeners.ResetJoinListener;
import com.mrpiepmatatzt.spawn.Core;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;


import java.io.File;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnPlugin extends JavaPlugin implements Listener {

    private Location spawnLocation;
    private Location spawn;
    public MessageUtil messages;
    private PickaxeManager pickaxeManager;
    private YamlConfiguration itemsConfig;
    private DeluxeCombatAPI dcApi;


    public YamlConfiguration getItemsConfig() {
        return itemsConfig;
    }

    // teleportTasks map
    private final Map<UUID, BukkitRunnable> teleportTasks = new HashMap<>();
    private final int teleportDelaySeconds = 5;

    public MessageUtil getMessages() {
        return messages;
    }

    public PickaxeManager getPickaxeManager() {
        return pickaxeManager;
    }

    public void setPickaxeManager(PickaxeManager pickaxeManager) {
        this.pickaxeManager = pickaxeManager;
    }

    @Override
    public void onEnable() {
        // Save default config.yml if not exists
        saveDefaultConfig();

        // Save items.yml to plugin folder if it doesn't exist yet
        saveResource("items.yml", false);

        File itemsFile = new File(getDataFolder(), "items.yml");
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        pickaxeManager = new PickaxeManager(itemsConfig);

        Plugin plugin = getServer().getPluginManager().getPlugin("DeluxeCombat");
        if (plugin != null) {
            try {
                // Use reflection to access the API instance
                java.lang.reflect.Method getApiMethod = plugin.getClass().getMethod("getAPI");
                dcApi = (DeluxeCombatAPI) getApiMethod.invoke(plugin);
                getLogger().info("DeluxeCombatAPI hooked successfully.");
            } catch (Exception e) {
                getLogger().warning("Failed to hook DeluxeCombatAPI: " + e.getMessage());
            }
        } else {
            getLogger().warning("DeluxeCombat plugin not found.");
        }




        // Load spawn location from config
        loadSpawn();

        // Register events
        getServer().getPluginManager().registerEvents(new ResetJoinListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new FirstJoinTeleportListener(this), this);
        pickaxeManager = new PickaxeManager(getItemsConfig()); // or however you load it
        getServer().getPluginManager().registerEvents(new MultiBreakListener(pickaxeManager), this);

        // Register commands
        if (getCommand("givepick") != null) {
            getCommand("givepick").setExecutor(new GivePickCommand(this));
        } else {
            getLogger().warning("Command 'givepick' not found in plugin.yml!");
        }

        if (getCommand("fakeop") != null) {
            getCommand("fakeop").setExecutor(new Core());
        } else {
            getLogger().warning("Command 'fakeop' not found in plugin.yml!");
        }

        if (getCommand("kings") != null) {
            getCommand("kings").setExecutor(new ReloadCommand(this));
        } else {
            getLogger().warning("Command 'kings' not found in plugin.yml!");
        }

        if (getCommand("stats") != null) {
            getCommand("stats").setExecutor(new StatsCommand(this));
        } else {
            getLogger().warning("Command 'stats' not found in plugin.yml!");
        }

        getLogger().info("king's main plugin has been enabled");
    }



    private void loadSpawnLocation() {
        if (getConfig().contains("spawn.world")) {
            World world = Bukkit.getWorld(getConfig().getString("spawn.world"));
            double x = getConfig().getDouble("spawn.x");
            double y = getConfig().getDouble("spawn.y");
            double z = getConfig().getDouble("spawn.z");
            float yaw = (float) getConfig().getDouble("spawn.yaw");
            float pitch = (float) getConfig().getDouble("spawn.pitch");

            if (world != null) {
                spawnLocation = new Location(world, x, y, z, yaw, pitch);
            } else {
                getLogger().warning("Spawn world not found!");
            }
        }
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (player.getBedSpawnLocation() == null) {
            if (spawnLocation != null) {
                event.setRespawnLocation(spawnLocation);
            } else {
                getLogger().warning("Spawn location is not set! Cannot teleport " + player.getName() + " on death.");
            }
        }
    }

    private void saveSpawnLocation(Location location) {
        getConfig().set("spawn.world", location.getWorld().getName());
        getConfig().set("spawn.x", location.getX());
        getConfig().set("spawn.y", location.getY());
        getConfig().set("spawn.z", location.getZ());
        getConfig().set("spawn.yaw", location.getYaw());
        getConfig().set("spawn.pitch", location.getPitch());
        saveConfig();
        spawnLocation = location;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("setspawn")) {
            if (!player.hasPermission("kings.spawn.set")) {
                player.sendMessage(color(getConfig().getString("no-permission")));
                return true;
            }
            saveSpawnLocation(player.getLocation());
            player.sendMessage(color(getConfig().getString("spawn-set")));
            return true;


        } else if (command.getName().equalsIgnoreCase("spawn")) {
            if (spawnLocation == null) {
                player.sendMessage(color(getConfig().getString("spawn-not-set")));
                return true;
            }

            if (!player.hasPermission("kings.spawn.teleport.self")) {
                player.sendMessage(color(getConfig().getString("no-permission")));
                return true;
            }

            if (args.length == 0) {
                // Combat check using DeluxeCombatAPI
                if (dcApi != null && dcApi.isInCombat(player) &&
                        !player.hasPermission("kings.spawn.combat.bypass")) {
                    player.sendMessage(ChatColor.RED + "You cannot teleport to spawn while in combat!");
                    return true;
                }

                if (player.hasPermission("kings.spawn.bypass")) {
                    player.teleport(spawnLocation);
                    playSound(player, "teleport");
                    player.sendActionBar(color(getConfig().getString("teleported-to-spawn-actionbar")));
                    playSound(player, "teleport2");
                } else {
                    startTeleportCountdown(player);
                }
                return true;
            }



            if (args.length == 1) {
                if (!player.hasPermission("kings.spawn.teleport.others")) {
                    player.sendMessage(color(getConfig().getString("no-permission")));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(color(getConfig().getString("player-not-found")));
                    return true;
                }

                target.teleport(spawnLocation);
                playSound(target, "teleport2");
                target.sendActionBar(color(getConfig().getString("teleported-to-spawn-actionbar")));
                player.sendMessage(color(getConfig().getString("teleported-other").replace("%player%", target.getName())));
                return true;
            }
        }

        return false;
    }

    private void startTeleportCountdown(Player player) {
        UUID uuid = player.getUniqueId();

        if (teleportTasks.containsKey(uuid)) {
            player.sendActionBar(color(getConfig().getString("teleport-cancelled")));
            return;
        }

        int countdown = 5;
        BukkitRunnable task = new BukkitRunnable() {
            int secondsLeft = countdown;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    teleportTasks.remove(uuid);
                    cancel();
                    return;
                }

                if (secondsLeft <= 0) {
                    player.teleport(spawnLocation);
                    playSound(player, "teleport");
                    player.sendActionBar(color(getConfig().getString("teleported-to-spawn-actionbar")));
                    playSound(player, "teleport2");
                    teleportTasks.remove(uuid);
                    cancel();
                    return;
                }

                playSound(player, "countdown");
                player.sendActionBar(color(getConfig().getString("teleport-countdown").replace("%seconds%", String.valueOf(secondsLeft))));
                secondsLeft--;
            }
        };

        teleportTasks.put(uuid, task);
        task.runTaskTimer(this, 0L, 20L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!teleportTasks.containsKey(uuid)) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            teleportTasks.get(uuid).cancel();
            teleportTasks.remove(uuid);
            player.sendActionBar(color(getConfig().getString("teleport-cancelled")));
            playSound(player, "cancelled");
        }
    }

    private void playSound(Player player, String soundKey) {
        String soundName = getConfig().getString("sounds." + soundKey);
        if (soundName != null && !soundName.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid sound: " + soundName);
            }
        }
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message != null ? message : "");
    }

    public Location getSpawn() {
        return this.spawn;
    }

    public void reloadItemsFile() {
        File itemsFile = new File(getDataFolder(), "items.yml");
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        pickaxeManager = new PickaxeManager(itemsConfig);
        getLogger().info("items.yml reloaded successfully.");
    }

    public void loadSpawn() {
        if (!getConfig().contains("spawn.world")) {
            getLogger().warning("Spawn location not set in config.yml!");
            return;
        }
        World world = Bukkit.getWorld(getConfig().getString("spawn.world"));
        double x = getConfig().getDouble("spawn.x");
        double y = getConfig().getDouble("spawn.y");
        double z = getConfig().getDouble("spawn.z");
        float yaw = (float) getConfig().getDouble("spawn.yaw");
        float pitch = (float) getConfig().getDouble("spawn.pitch");

        if (world == null) {
            getLogger().warning("World for spawn location not found!");
            return;
        }

        spawnLocation = new Location(world, x, y, z, yaw, pitch);  // <--- assign here, not spawn
    }
}
