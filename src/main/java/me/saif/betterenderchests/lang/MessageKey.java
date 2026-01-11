package me.saif.betterenderchests.lang;

import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

public enum MessageKey {

    EC_COMMAND_NO_PERMISSION_SELF("configurable.enderchest-command-messages.no-permission-self", "&cYou do not have permission to open your enderchest via command."),
    EC_COMMAND_NO_PERMISSION_OTHERS("configurable.enderchest-command-messages.no-permission-others", "&cYou do not have permission to view the enderchest of others."),
    EC_COMMAND_WORLD_DISABLED("configurable.enderchest-command-messages.world-disabled", "&cThis command is disabled in this world"),
    COMMAND_CONSOLE_ONLY("configurable.command-console-only", "&cYou can only use this command from the console."),
    COMMAND_PLAYER_ONLY("internal.command.command-player-only", "You must be a player to use this command"),
    NO_ENDERCHEST_SELF("configurable.no-rows", "&cYou do not have an enderchest."),
    NO_ENDERCHEST_OTHER("configurable.no-enderchest-found", "&c<player> does not have an enderchest."),
    COMMAND_USAGE("configurable.command-usage", "&cUsage: <command>"),
    BLACKLIST_MESSAGE("configurable.blacklisted-message", "&cYou cannot put that item into an ender chest."),

    ENDERCHEST_1_ROWS("configurable.enderchest-names.1-rows","&7<player>'s Enderchest"),
    ENDERCHEST_2_ROWS("configurable.enderchest-names.2-rows","&7<player>'s Enderchest"),
    ENDERCHEST_3_ROWS("configurable.enderchest-names.3-rows","&7<player>'s Enderchest"),
    ENDERCHEST_4_ROWS("configurable.enderchest-names.4-rows","&7<player>'s Enderchest"),
    ENDERCHEST_5_ROWS("configurable.enderchest-names.5-rows","&7<player>'s Enderchest"),
    ENDERCHEST_6_ROWS("configurable.enderchest-names.6-rows","&7<player>'s Enderchest"),

    ALL_PLAYERS_OFFLINE("internal.converter.all-players-offline", "All players must be offline to use this command"),
    NOT_VALID_CONVERTER("internal.converter.not-valid-conveter", "That is not a valid converter."),
    LIST_VALID_CONVERTERS("internal.converter.list-valid-convenrters", "The valid converters are listed below"),
    CONFIRM_CONVERTER("internal.converter.confirm-converter", "You need to execute this command again to be sure that you would like to convert.",
            "This may result in everyone online being kicked from the server or major lag.",
            "This will also result in the loss of all data being stored by this plugin",
            "Proceed with caution!"),
    CONVERSION_STARTING("internal.converter.conversion-start", "Conversion from <converter> is starting."),
    CONVERSION_SUCCESS("internal.converter.conversion-success", "Conversion successfully finished"),
    CONVERSION_FAILURE("internal.converter.conversion-failure", "Conversion failed"),

    CLEARED_ENDERCHEST("internal.enderchest.cleared-enderchest", "Cleared the enderchest of <player>"),
    CONFIRM_CLEAR_ENDERCHEST("internal.enderchest.confirm-clear", "Are you sure you wish to clear the enderchest of <player>",
            "Run this command again within <seconds> seconds to confirm"),

    BLACKLIST_INVALID_ITEM("internal.blacklist.invalid-type", "Blacklisted item by the name of <type> found. This is not a valid minecraft material. Ignoring..."),
    ENDERCHEST_CONSOLE_USAGE("internal.command.enderchest-console-usage", "Usage: /enderchest <Player> [(Optional)Other Player] - Opens the enderchest for 'Player'. If 'Other Player' is specified, The enderchest of 'Other Player' will be opened for 'Player'"),
    PLAYER_NEEDED_ONLINE("internal.command.player-needed-online", "<player> must be online to run that command."),
    CONSOLE_OPENED_ENDERCHEST("internal.command.console-opened-enderchest", "Opened <target>'s enderchest for <player>");

    private final String path;
    private final String[] def;
    private final String[] translated;

    MessageKey(String path, String... def) {
        this.path = path;
        this.def = def;
        this.translated = Arrays.stream(def).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toArray(String[]::new);
    }

    public String getPath() {
        return path;
    }

    public String[] getDefault() {
        return def;
    }

    public String[] getTranslated() {
        return translated;
    }
}
