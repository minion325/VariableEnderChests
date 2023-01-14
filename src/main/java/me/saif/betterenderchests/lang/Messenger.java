package me.saif.betterenderchests.lang;

import me.saif.betterenderchests.lang.locale.Locale;
import me.saif.betterenderchests.lang.locale.LocaleLoader;
import me.saif.betterenderchests.lang.locale.PlayerLocaleFinder;
import me.saif.betterenderchests.lang.placeholder.PlaceholderResult;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messenger {

    private LocaleLoader lm;
    private PlayerLocaleFinder pll;

    public Messenger(LocaleLoader lm, PlayerLocaleFinder pll) {
        this.lm = lm;
        this.pll = pll;
    }

    public String[] getMessage(CommandSender sender, MessageKey messageKey, PlaceholderResult... placeholders) {
        Locale locale = !(sender instanceof Player) ? lm.getDefaultLocale() : pll.getLocale(((Player) sender));

        return locale.getFormattedMessage(messageKey, placeholders);
    }

    public void sendMessage(CommandSender sender, MessageKey messageKey, PlaceholderResult... placeholders) {
        for (String s : getMessage(sender, messageKey, placeholders)) {
            sender.sendMessage(s);
        }
    }


}
