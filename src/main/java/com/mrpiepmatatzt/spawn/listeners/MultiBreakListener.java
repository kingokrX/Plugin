package com.mrpiepmatatzt.spawn.listeners;

import com.mrpiepmatatzt.spawn.items.PickaxeManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.flags.Flags;

public class MultiBreakListener implements Listener {

    private final PickaxeManager pickaxeManager;

    public MultiBreakListener(PickaxeManager pickaxeManager) {
        this.pickaxeManager = pickaxeManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;

        // Check if the item is one of our special pickaxes
        String type = pickaxeManager.getPickaxeType(item);
        if (type == null) return;

        int radius = pickaxeManager.getRadius(type);
        if (radius <= 0) radius = 1; // fallback to at least 1 (just the block)

        Block mainBlock = event.getBlock();

        RegionManager regionManager = WorldGuard.getInstance().getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));
        if (regionManager == null) return;

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        int half = radius / 2;

        // Loop through blocks in radius (cube, e.g., 3x3x3)
        for (int x = -half; x <= half; x++) {
            for (int y = -half; y <= half; y++) {
                for (int z = -half; z <= half; z++) {
                    Block currentBlock = mainBlock.getRelative(x, y, z);
                    if (currentBlock.equals(mainBlock)) continue;

                    // Skip unmineable blocks
                    if (pickaxeManager.getUnmineableBlocks().contains(currentBlock.getType())) continue;

                    Location wgLoc = BukkitAdapter.adapt(currentBlock.getLocation());
                    ApplicableRegionSet set = regionManager.getApplicableRegions(wgLoc.toVector().toBlockPoint());

                    // Check if player can build here
                    if (!set.testState(localPlayer, Flags.BUILD)) continue;

                    currentBlock.breakNaturally(item);
                }
            }
        }
    }
}

