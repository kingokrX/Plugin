package com.mrpiepmatatzt.spawn.gui;

import com.mrpiepmatatzt.spawn.SpawnPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerSelectGUI implements Listener {

    private final SpawnPlugin plugin;
    private final Player viewer;
    private final Inventory gui;
    private final String guiTitle;

    public PlayerSelectGUI(SpawnPlugin plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.guiTitle = ChatColor.DARK_AQUA + "Select a player to reset";
        this.gui = Bukkit.createInventory(null, 27, guiTitle);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (slot >= 27) break;

            ItemStack head = createPlayerHead(online);
            gui.setItem(slot++, head);
        }
        viewer.openInventory(gui);
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.GREEN + player.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to reset this player");
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(guiTitle)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getWhoClicked().equals(viewer)) return;
        if (event.getClickedInventory() == null) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;

        SkullMeta meta = (SkullMeta) clicked.getItemMeta();
        OfflinePlayer target = meta.getOwningPlayer();
        if (target == null) {
            viewer.sendMessage(ChatColor.RED + "Could not find player data.");
            viewer.closeInventory();
            return;
        }

        viewer.closeInventory();

        // Open reset options GUI
        new ResetOptionsGUI(plugin, viewer, target).open();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(guiTitle)) return;
        if (!event.getPlayer().equals(viewer)) return;

        HandlerList.unregisterAll(this); // unregister listener
    }
}
