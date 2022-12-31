package me.saif.betterenderchests.lang.locale;

import me.saif.betterenderchests.lang.MessageKey;
import me.saif.betterenderchests.lang.placeholder.PlaceholderResult;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Locale {

    public static final char SEPARATOR = '_';

    private String lang;
    private Set<String> countries;

    private Map<MessageKey, String[]> messages;

    public Locale(String lang, Set<String> countries, Map<MessageKey, String[]> messages) {
        this.lang = lang;
        this.countries = countries.stream().map(s -> s.toLowerCase(java.util.Locale.ROOT)).collect(Collectors.toSet());
        this.messages = new ConcurrentHashMap<>();
        messages.forEach((key, stringArray) -> {
            this.messages.put(key, Arrays.stream(stringArray).map(msgLine -> ChatColor.translateAlternateColorCodes('&', msgLine)).toArray(String[]::new));
        });
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

    public String getLang() {
        return lang;
    }

    public Set<String> getCountries() {
        return new HashSet<>(countries);
    }

    public Set<String> getLocales() {
        return countries.stream().map(s -> lang + SEPARATOR + s).collect(Collectors.toSet());
    }

}
