package me.saif.betterenderchests.lang;

public enum MessageKey {

    COMMAND_NO_PERMISSION_SELF      ("configurable.command-permission-self", "&4You do not have permission to use this command."),
    COMMAND_NO_PERMISSION_OTHERS    ("configurable.command-permission-others", "&4You do not have permission to view the enderchest of others."),
    NO_ENDERCHEST_SELF              ("configurable.no-rows", "&4You do not have an enderchest."),
    NO_ENDERCHEST_OTHER             ("configurable.no-enderchest-found", "&4<player> does not have an enderchest."),
    BLACKLIST_MESSAGE               ("configurable.blacklisted-message", "&4You cannot put that item into an ender chest."),

    ALL_PLAYERS_OFFLINE             ("internal.converter.all-players-offline", "All players must be offline to use this command"),
    NOT_VALID_CONVERTER             ("internal.converter.not-valid-conveter", "That is not a valid converter."),
    LIST_VALID_CONVERTERS           ("internal.converter.list-valid-convenrters", "The valid converters are listed below"),
    CONFIRM_CONVERTER               ("internal.converter.confirm-converter", "You need to execute this command again to be sure that you would like to convert.",
                                                                                     "This may result in everyone online being kicked from the server or major lag.",
                                                                                     "This will also result in the loss of all data being stored by this plugin",
                                                                                     "Proceed with caution!"),
    CONVERSION_STARTING             ("internal.converter.conversion-start", "Conversion from <converter> is starting."),
    CONVERSION_SUCCESS              ("internal.converter.conversion-success", "Conversion successfully finished"),
    CONVERSION_FAILURE              ("internal.converter.conversion-failure", "Conversion failed"),

    CLEARED_ENDERCHEST              ("internal.enderchest.cleared-enderchest", "Cleared the enderchest of <player>"),
    CONFIRM_CLEAR_ENDERCHEST        ("internal.enderchest.confirm-clear", "Are you sure you wish to clear the enderchest of <player>",
                                                                                    "Run this command again within <seconds> seconds to confirm"),

    BLACKLIST_INVALID_ITEM          ("internal.blacklist.invalid-type", "Blacklisted item by the name of <type> found. This is not a valid minecraft material. Ignoring...");
    private String path;
    private String[] def;

    MessageKey(String path, String... def) {
        this.path = path;
        this.def = def;
    }

    public String getPath() {
        return path;
    }

    public String[] getDefault() {
        return def;
    }
}
