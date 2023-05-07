package me.saif.betterenderchests.converters.enderplus;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.converters.Converter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class EnderPlusConverter extends Converter {

    public EnderPlusConverter(VariableEnderChests plugin, String name) {
        super(plugin, name);
    }

    protected FileConfiguration getConfig() throws IOException, InvalidConfigurationException {
        File file = new File(this.plugin.getDataFolder().getParentFile(), "EnderPlus");
        if (!file.isDirectory())
            return null;

        file = new File(file, "data.yml");

        if (!file.exists())
            return null;

        FileConfiguration fileConfiguration = new YamlConfiguration();
        fileConfiguration.load(file);

        return fileConfiguration;

    }

}
