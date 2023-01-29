package me.saif.betterenderchests.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.saif.betterenderchests.VariableEnderChests;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class PAPIEnderChestHook extends PlaceholderExpansion {

    private final VariableEnderChests plugin;

    public PAPIEnderChestHook(VariableEnderChests plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().stream().findFirst().orElse("None!");
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null)
            return null;

        if (params.equalsIgnoreCase("size"))
            return String.valueOf(plugin.getEnderChestManager().getNumRows(player) * 9);

        else if (params.equalsIgnoreCase("rows")) {
            return String.valueOf(plugin.getEnderChestManager().getNumRows(player));
        }

        return null;
    }
}
