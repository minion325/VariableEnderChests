package me.saif.betterenderchests.data;

import me.saif.betterenderchests.VariableEnderChests;

import java.util.Arrays;

public class ConfigUpdater {

    private VariableEnderChests plugin;

    private final int latest = 2;

    private int current;

    public ConfigUpdater(VariableEnderChests plugin) {
        this.plugin = plugin;

        this.current = this.plugin.getConfig().getInt("config-version");

        while (current < latest) {
            update();
        }
    }

    private void update() {
        //updates from 1 to 2
        this.plugin.getLogger().info("Updating config from version " + this.current + " to " + (this.current + 1));
        if (this.plugin.getVersion() < 18) {
            this.plugin.getConfig().options().header("You are using an older version of minecraft so comments have been deleted by updating the config\n" +
                    "Check out https://github.com/minion325/VariableEnderChests/blob/master/src/main/resources/config.yml to see the config.yml with comments\n" +
                    "Do not touch config-version. This is automatically updated by the plugin.");
        }
        if (this.current == 1) {
            this.plugin.getConfig().set("config-version", 2);

            for (int i = 1; i <= 6; i++) {
                this.plugin.getConfig().set("enderchest-names." + i + "-rows", "&7<player>'s Enderchest");
            }
            if (this.plugin.getVersion() >= 18) {
                this.plugin.getConfig().setComments("enderchest-names", Arrays.asList("These are the inventory names that players will see when they open their inventory",
                        "You can set a different name for each size eg. Level 1, Level 2",
                        "<player> is replaced with the player's name"));
            }
            this.current++;
            this.plugin.saveConfig();
        }
    }


}
