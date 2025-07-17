package com.mrpiepmatatzt.spawn;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;

public class MessageUtil {

    private final FileConfiguration config;

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    public MessageUtil(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Sends a message from config with optional placeholders and action bar toggle
     */
    public void send(Player player, String path, Map<String, String> placeholders, boolean actionBar) {
        String raw = config.getString(path);
        if (raw == null) {
            player.sendMessage("§cMessage path '" + path + "' not found in config.");
            return;
        }

        // Apply placeholders
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                raw = raw.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        Component component = parse(raw);

        if (actionBar) {
            player.sendActionBar(component);
        } else {
            player.sendMessage(component);
        }
    }

    /**
     * Sends a message to any CommandSender (no placeholders or action bar)
     */
    public void send(CommandSender sender, String path) {
        String raw = config.getString(path);
        if (raw == null) {
            sender.sendMessage("§cMessage path '" + path + "' not found in config.");
            return;
        }

        Component component = parse(raw);
        sender.sendMessage(component);
    }

    /**
     * Send with placeholders (chat only)
     */
    public void send(Player player, String path, Map<String, String> placeholders) {
        send(player, path, placeholders, false);
    }

    /**
     * Send without placeholders (chat only)
     */
    public void send(Player player, String path) {
        send(player, path, null, false);
    }

    /**
     * Parses string to Component using legacy + MiniMessage
     */
    public static Component parse(String msg) {
        if (msg == null) return Component.empty();

        // Convert legacy hex (&x&F&F&0&0&F&F) to MiniMessage format
        msg = convertLegacyHex(msg);

        // Translate legacy color codes (&a, &l, etc.)
        msg = LEGACY.serialize(LEGACY.deserialize(msg));

        // Deserialize as MiniMessage
        return MINI.deserialize(msg);
    }

    /**
     * Converts legacy &x hex colors to <#hex> MiniMessage format
     */
    private static String convertLegacyHex(String message) {
        if (!message.contains("&x")) return message;

        StringBuilder result = new StringBuilder();
        char[] chars = message.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (i + 13 < chars.length && chars[i] == '&' && chars[i + 1] == 'x') {
                StringBuilder hex = new StringBuilder();
                for (int j = 2; j < 14; j += 2) {
                    if (chars[i + j] == '&') hex.append(chars[i + j + 1]);
                }
                result.append("<#").append(hex).append(">");
                i += 13;
            } else {
                result.append(chars[i]);
            }
        }

        return result.toString();
    }
}
