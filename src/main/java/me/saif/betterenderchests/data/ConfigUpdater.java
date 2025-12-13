package me.saif.betterenderchests.data;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import me.saif.betterenderchests.VariableEnderChests;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class ConfigUpdater {

    private VariableEnderChests plugin;

    private final int latest = 7;

    private int current;

    public ConfigUpdater(VariableEnderChests plugin) {
        this.plugin = plugin;

        this.current = this.plugin.getConfig().getInt("config-version");

        if (current < latest && !MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_18_R1)) {
            this.plugin.getConfig().options().header("You are using an older version of minecraft so comments have been deleted by updating the config\n" +
                    "Check out https://github.com/minion325/VariableEnderChests/blob/master/src/main/resources/config.yml to see the config.yml with comments\n" +
                    "Do not touch config-version. This is automatically updated by the plugin.");
        }
        while (current < latest) {
            update();
        }
    }

    private void update() {
        this.plugin.getLogger().info("Updating config from version " + this.current + " to " + (this.current + 1));

        //updates from 1 to 2
        if (this.current == 1) {
            this.plugin.getConfig().set("config-version", 2);

            for (int i = 1; i <= 6; i++) {
                this.plugin.getConfig().set("enderchest-names." + i + "-rows", "&7<player>'s Enderchest");
            }
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_18_R1)) {
                this.plugin.getConfig().setComments("enderchest-names", Arrays.asList("These are the inventory names that players will see when they open their inventory",
                        "You can set a different name for each size eg. Level 1, Level 2",
                        "<player> is replaced with the player's name"));
            }
            this.current++;
            this.plugin.saveConfig();
        }

        //frm 2 to 3
        if (this.current == 2) {
            this.plugin.getConfig().set("config-version", 3);

            this.plugin.getConfig().set("blacklisted-message", "&4You cannot put that item into an ender chest.");
            this.plugin.getConfig().set("blacklisted-items", Arrays.asList("COMMAND_BLOCK"));
            this.current++;
            this.plugin.saveConfig();
        }

        //from 3 -> 4
        if (this.current == 3) {
            this.plugin.getConfig().set("config-version", 4);

            this.plugin.getConfig().set("database.mysql", false);
            this.plugin.getConfig().set("database.host", "localhost");
            this.plugin.getConfig().set("database.port", 3306);
            this.plugin.getConfig().set("database.database", "database");
            this.plugin.getConfig().set("database.username", "username");
            this.plugin.getConfig().set("database.password", "password");

            this.current++;
            this.plugin.saveConfig();
        }

        //from 4->5
        if (this.current == 4) {
            try {
                this.plugin.getLogger().info("Backing up your version 4 config before updating to version 5");
                this.plugin.getConfig().save(new File(this.plugin.getDataFolder(), "config_v4.yml"));
            } catch (IOException e) {
                this.plugin.getLogger().severe("Could not back up old config.");
                throw new RuntimeException(e);
            }
            this.plugin.getConfig().set("config-version", 5);
            this.plugin.getConfig().set("enderchest-names", null);
            this.plugin.getConfig().set("command-permission-self", null);
            this.plugin.getConfig().set("command-permission-others", null);
            this.plugin.getConfig().set("no-enderchest-found", null);
            this.plugin.getConfig().set("no-rows", null);
            this.plugin.getConfig().set("blacklisted-message", null);
            this.plugin.getConfig().set("default-locale", "en_us");
            this.plugin.getConfig().set("open-enderchest-commands", Arrays.asList("enderchest", "echest"));
            this.current++;
            this.plugin.saveConfig();
        }

        //from 5 -> 6
        if (this.current == 5) {
            this.plugin.getConfig().set("config-version", 6);
            this.plugin.getConfig().set("papi-identifier", this.plugin.getName().toLowerCase(Locale.ENGLISH));

            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_18_R1)) {
                this.plugin.getConfig().setComments("papi-identifier", Arrays.asList(
                        "This is used when the plugin hooks into placeholder api to provide placeholders",
                        "This allows you to modify the placeholder identifier.",
                        "Placeholders can be parsed as %<papi-identifier>_size%"));
            }

            this.current++;

            this.plugin.saveConfig();
        }

        //from 6 -> 7
        if (this.current == 6) {
            this.plugin.getConfig().set("config-version", 7);
            this.plugin.getConfig().set("disabled-worlds", Arrays.asList(
                    "some_world_here",
                    "some_world_here_nether",
                    "some_world_here_the_end"
            ));

            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_18_R1)) {
                this.plugin.getConfig().setComments("disabled-worlds", Arrays.asList(
                        "Any worlds specified here will not have access to the Variable Ender Chest",
                        "Players will not be able to access it via command OR block.",
                        "Instead accessing blocks will open the default vanilla enderchest."));
            }

            this.current++;

            this.plugin.saveConfig();
        }
    }


}
