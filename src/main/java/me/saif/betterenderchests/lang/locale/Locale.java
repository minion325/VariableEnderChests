package me.saif.betterenderchests.lang.locale;

import me.saif.betterenderchests.lang.MessageKey;
import me.saif.betterenderchests.lang.placeholder.PlaceholderResult;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Locale {

    private String localeCode;
    private String[] others;

    private Map<MessageKey, String[]> messages;

    public Locale(String localeCode, Map<MessageKey, String[]> messages, String... otherCodes) {
        this.localeCode = localeCode;
        this.others = otherCodes;
        this.messages = new HashMap<>();
        messages.forEach((key, stringArray) -> messages.put(key, ((String[]) Arrays.stream(stringArray).map(msgLine -> ChatColor.translateAlternateColorCodes('&', msgLine)).toArray())));
    }

    public String[] getMessage(MessageKey key) {
        return messages.getOrDefault(key, key.getDefault());
    }

    public String[] getFormattedMessage(MessageKey key, PlaceholderResult... placeholderResults) {
        String[] messages = getMessage(key);
        String[] toReturn = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            String s = messages[i];
            for (PlaceholderResult placeholderResult : placeholderResults) {
                s = placeholderResult.replace(s);
            }
            toReturn[i] = s;
        }
        return toReturn;
    }

    public String getLocaleCode() {
        return localeCode;
    }
}
