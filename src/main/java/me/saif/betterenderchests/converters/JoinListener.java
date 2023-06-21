package me.saif.betterenderchests.converters;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class JoinListener implements Listener {

    private ConverterManager converterManager;

    public JoinListener(ConverterManager converterManager) {
        this.converterManager = converterManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onJoin(AsyncPlayerPreLoginEvent event) {
        if (!this.converterManager.isConverting())
            return;

        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        event.setKickMessage("Server not currently join able.");
    }

}
