package me.saif.betterenderchests.data;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class Messages {

    public static final String COMMAND_NO_PERMISSION_SELF = "command-permission-self";
    public static final String COMMAND_NO_PERMISSION_OTHERS = "command-permission-others";
    public static final String NO_ROWS = "no-rows";
    public static final String NO_ENDERCHEST_FOUND = "no-enderchest-found";
    public static final String BLACKLIST_MESSAGE = "blacklisted-message";

    private Map<String, String> messages = new HashMap<>();

    public Messages(FileConfiguration config) {
        this.messages.put(COMMAND_NO_PERMISSION_OTHERS, colorify(config.getString(COMMAND_NO_PERMISSION_OTHERS)));
        this.messages.put(COMMAND_NO_PERMISSION_SELF, colorify(config.getString(COMMAND_NO_PERMISSION_SELF)));
        this.messages.put(NO_ROWS, colorify(config.getString(NO_ROWS)));
        this.messages.put(NO_ENDERCHEST_FOUND, colorify(config.getString(NO_ENDERCHEST_FOUND)));
        this.messages.put(BLACKLIST_MESSAGE, colorify(config.getString(BLACKLIST_MESSAGE)));
    }

    private String colorify(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public void sendTo(CommandSender sender, String message) {
        String msg = this.messages.get(message);
        if (msg == null)
            return;
        sender.sendMessage(msg);
    }

}
