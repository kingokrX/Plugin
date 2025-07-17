package com.mrpiepmatatzt.spawn.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String colorize(String message) {
        // Translate hex colors
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hexColor).toString());
        }
        matcher.appendTail(buffer);

        // Replace '&' with '§'
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
