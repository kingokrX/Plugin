package com.mrpiepmatatzt.spawn.gui;

import com.mrpiepmatatzt.spawn.SpawnPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.mrpiepmatatzt.spawn.util.ColorUtil;

import java.io.File;

public class ConfirmResetGUI implements Listener {

    private final SpawnPlugin plugin;
    private final Player viewer;
    private final OfflinePlayer target;
    private final Inventory gui;
    private final String guiTitle = ChatColor.DARK_GREEN + "Confirm reset";
    private final ResetOptionsGUI.ResetType resetType;

    public ConfirmResetGUI(SpawnPlugin plugin, Player viewer, OfflinePlayer target, ResetOptionsGUI.ResetType resetType) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.target = target;
        this.resetType = resetType;

        this.gui = Bukkit.createInventory(null, 27, guiTitle);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        gui.setItem(15, createItem(Material.LIME_WOOL, ChatColor.GREEN + "Confirm"));
        gui.setItem(13, createItem(Material.PAPER, ChatColor.YELLOW + getConfirmDescription()));
        gui.setItem(11, createItem(Material.RED_WOOL, ChatColor.RED + "Cancel"));

        viewer.openInventory(gui);
    }

    private String getConfirmDescription() {
        switch (resetType) {
            case PLAYERDATA:
                return "Reset playerdata for " + target.getName();
            case STATS:
                return "Reset stats for " + target.getName();
            case BOTH:
                return "Reset both playerdata and stats for " + target.getName();
            case NEW_SEASON:
                return "Reset all players data and stats";
            default:
                return "";
        }
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(guiTitle)) return;
        if (!event.getWhoClicked().equals(viewer)) return;
        if (event.getClickedInventory() == null) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        Material mat = clicked.getType();

        if (mat == Material.LIME_WOOL) {
            viewer.closeInventory();
            performReset();
            HandlerList.unregisterAll(this);
        } else if (mat == Material.RED_WOOL) {
            viewer.sendMessage(ChatColor.YELLOW + "Reset cancelled.");
            viewer.closeInventory();
            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(guiTitle)) return;
        if (!event.getPlayer().equals(viewer)) return;

        HandlerList.unregisterAll(this);
    }

    private void performReset() {
        switch (resetType) {
            case PLAYERDATA:
                resetPlayerData(target);
                break;
            case STATS:
                resetStats(target);
                break;
            case BOTH:
                resetPlayerData(target);
                resetStats(target);
                break;
            case NEW_SEASON:
                resetAllPlayers();
                break;
        }
    }

    private void resetPlayerData(OfflinePlayer player) {
        boolean wasOnline = player.isOnline();

        if (wasOnline) {
            ((Player) player).kickPlayer(ChatColor.RED + "Your player data is being reset...");
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            File worldFolder = plugin.getServer().getWorlds().get(0).getWorldFolder();
            File playerDataFolder = new File(worldFolder, "playerdata");
            String uuid = player.getUniqueId().toString();
            boolean deleted = false;

            if (playerDataFolder.exists()) {
                for (File file : playerDataFolder.listFiles()) {
                    if (file.getName().contains(uuid)) {
                        if (file.delete()) {
                            deleted = true;
                        }
                    }
                }
            }

            if (deleted) {
                viewer.sendMessage(ChatColor.GREEN + "Playerdata reset for " + player.getName());
            } else {
                viewer.sendMessage(ChatColor.RED + "No playerdata files found for " + player.getName());
            }

            try {
                File marker = new File(plugin.getDataFolder(), "reset-temp/" + player.getUniqueId());
                marker.getParentFile().mkdirs();
                marker.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2L);
    }

    private void resetStats(OfflinePlayer player) {
        if (player.isOnline()) {
            ((Player) player).kickPlayer(ChatColor.RED + "Your stats have been reset.");
        }

        File statsFolder = new File(plugin.getDataFolder(), "stats");
        String uuid = player.getUniqueId().toString();
        boolean deleted = false;

        if (statsFolder.exists()) {
            for (File file : statsFolder.listFiles()) {
                if (file.getName().contains(uuid)) {
                    if (file.delete()) {
                        deleted = true;
                    }
                }
            }
        }

        if (deleted) {
            viewer.sendMessage(ChatColor.GREEN + "Stats reset for " + player.getName());
        } else {
            viewer.sendMessage(ChatColor.RED + "No stats files found for " + player.getName());
        }
    }

    private void resetAllPlayers() {
        // Delete playerdata
        File playerDataFolder = new File(plugin.getServer().getWorlds().get(0).getWorldFolder(), "playerdata");
        if (playerDataFolder.exists()) {
            for (File file : playerDataFolder.listFiles()) {
                file.delete();
            }
        }

        // Delete internal plugin stats folder
        File statsFolder = new File(plugin.getDataFolder(), "stats");
        if (statsFolder.exists()) {
            for (File file : statsFolder.listFiles()) {
                file.delete();
            }
        }

        // Delete external plugin files/folders
        deletePaths(
                "plugins/BetterTeams/teaminfo",
                "plugins/BetterTeams/team.yml",
                "plugins/BetterTeams/teams.yml",
                "plugins/BetterTeams/teamsBACKUP.yml",
                "plugins/Essentials/userdata",
                "plugins/LifeStealZ/userData.db",
                "plugins/ARedstoneDisable/redstone_logs.txt",
                "world/playerdata",
                "world/stats"
        );

        // Shut down the server after everything is deleted
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.kickPlayer("");  // Kick with empty message
            }
            Bukkit.shutdown();
        });
    }

    private void deletePaths(String... paths) {
        for (String pathString : paths) {
            File file = new File(pathString);

            if (!file.exists()) continue;

            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
    }

    private void deleteDirectory(File dir) {
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
