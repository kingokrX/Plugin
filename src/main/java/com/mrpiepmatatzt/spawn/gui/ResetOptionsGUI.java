package com.mrpiepmatatzt.spawn.gui;
import com.mrpiepmatatzt.spawn.util.ColorUtil;

import com.mrpiepmatatzt.spawn.SpawnPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ResetOptionsGUI implements Listener {

    private final SpawnPlugin plugin;
    private final Player viewer;
    private final OfflinePlayer target;
    private final String guiTitle;
    private Inventory gui;

    public ResetOptionsGUI(SpawnPlugin plugin, Player viewer, OfflinePlayer target) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.target = target;
        this.guiTitle = ChatColor.RED + "Reset options for " + target.getName();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        gui = Bukkit.createInventory(null, 9, guiTitle);

        gui.setItem(2, createItem(Material.PLAYER_HEAD, ChatColor.YELLOW + "player data"));
        gui.setItem(4, createItem(Material.YELLOW_DYE, ChatColor.GOLD + "reset stat"));
        gui.setItem(6, createItem(Material.BLUE_CONCRETE, ChatColor.RED + "reset both"));


        viewer.openInventory(gui);
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @org.bukkit.event.EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(guiTitle)) return;
        if (!event.getWhoClicked().equals(viewer)) return;
        if (event.getClickedInventory() == null) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
        ResetType resetType;

        if (displayName.contains("player data")) {
            resetType = ResetType.PLAYERDATA;
        } else if (displayName.contains("stat")) { // This still works with "Stat's Gang"
            resetType = ResetType.STATS;
        } else if (displayName.contains("both")) {
            resetType = ResetType.BOTH;
        } else {
            return;
        }

        viewer.closeInventory();
        new ConfirmResetGUI(plugin, viewer, target, resetType).open();
    }

    @org.bukkit.event.EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(guiTitle)) return;
        if (!event.getPlayer().equals(viewer)) return;

        HandlerList.unregisterAll(this);
    }

    public enum ResetType {
        PLAYERDATA,
        STATS,
        BOTH,
        NEW_SEASON // also used in ConfirmResetGUI
    }
}
