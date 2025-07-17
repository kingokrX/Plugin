    package com.mrpiepmatatzt.spawn.items;
    import com.mrpiepmatatzt.spawn.util.ColorUtil;

    import org.bukkit.Material;
    import org.bukkit.configuration.ConfigurationSection;
    import org.bukkit.configuration.file.FileConfiguration;
    import org.bukkit.entity.Player;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.inventory.meta.ItemMeta;

    import java.util.*;

    public class PickaxeManager {

        private final Map<String, PickaxeConfig> pickaxeConfigs = new HashMap<>();
        private final Set<Material> unmineableBlocks = new HashSet<>();

        public PickaxeManager(FileConfiguration config) {
            loadPickaxes(config);
            // Define unmineable blocks
            unmineableBlocks.add(Material.BEDROCK);
            unmineableBlocks.add(Material.STRUCTURE_VOID);
            // Add more if needed
        }

        private void loadPickaxes(FileConfiguration config) {
            ConfigurationSection section = config.getConfigurationSection("pickaxes");
            if (section == null) return;

            for (String key : section.getKeys(false)) {
                ConfigurationSection pickaxeSection = section.getConfigurationSection(key);
                if (pickaxeSection == null) continue;

                Material material = Material.matchMaterial(pickaxeSection.getString("material", "NETHERITE_PICKAXE"));
                String name = ColorUtil.colorize(pickaxeSection.getString("name", "&bPickaxe"));
                List<String> loreRaw = pickaxeSection.getStringList("lore");
                List<String> lore = new ArrayList<>();
                for (String line : loreRaw) {
                    lore.add(ColorUtil.colorize(line));
                }

                boolean unbreakable = pickaxeSection.getBoolean("unbreakable", false);
                int radius = pickaxeSection.getInt("radius", 1);
                int quantity = pickaxeSection.getInt("quantity", 1);

                Map<String, Integer> enchantments = new HashMap<>();
                ConfigurationSection enchSection = pickaxeSection.getConfigurationSection("enchantments");
                if (enchSection != null) {
                    for (String enchKey : enchSection.getKeys(false)) {
                        enchantments.put(enchKey.toUpperCase(), enchSection.getInt(enchKey, 1));
                    }
                }

                if (material != null) {
                    pickaxeConfigs.put(key.toLowerCase(), new PickaxeConfig(material, name, lore, unbreakable, enchantments, radius, quantity));
                }
            }
        }

        public void givePickaxe(String type, Player player) {
            PickaxeConfig config = pickaxeConfigs.get(type.toLowerCase());
            if (config == null) {
                player.sendMessage("§cUnknown pickaxe type: " + type);
                return;
            }

            ItemStack pickaxe = new ItemStack(config.itemType(), config.quantity());
            ItemMeta meta = pickaxe.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(config.name());
                meta.setLore(config.lore());
                meta.setUnbreakable(config.unbreakable());

                config.enchantments().forEach((enchant, level) -> {
                    try {
                        meta.addEnchant(org.bukkit.enchantments.Enchantment.getByName(enchant), level, true);
                    } catch (Exception ignored) {
                    }
                });

                pickaxe.setItemMeta(meta);
            }

            player.getInventory().addItem(pickaxe);
            player.sendMessage("§aYou have been given " + config.quantity() + "x " + config.name() + "!");
        }

        public String getPickaxeType(ItemStack item) {
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return null;

            String displayName = item.getItemMeta().getDisplayName();
            for (Map.Entry<String, PickaxeConfig> entry : pickaxeConfigs.entrySet()) {
                if (displayName.equals(entry.getValue().name())) {
                    return entry.getKey();
                }
            }
            return null;
        }

        public int getRadius(String type) {
            PickaxeConfig config = pickaxeConfigs.get(type.toLowerCase());
            return config != null ? config.radius() : 1;
        }

        public Set<Material> getUnmineableBlocks() {
            return Collections.unmodifiableSet(unmineableBlocks);
        }

        // This is the important method you were missing
        public Set<String> getPickaxeTypes() {
            return Collections.unmodifiableSet(pickaxeConfigs.keySet());
        }

        private record PickaxeConfig(
                Material itemType,
                String name,
                List<String> lore,
                boolean unbreakable,
                Map<String, Integer> enchantments,
                int radius,
                int quantity) {
        }

        @Override
        public String toString() {
            return "PickaxeManager{" +
                    "types=" + pickaxeConfigs.keySet() +
                    '}';
        }

        public void reload(FileConfiguration config) {
            pickaxeConfigs.clear(); // Clear old data
            loadPickaxes(config);   // Load new data
        }
    }