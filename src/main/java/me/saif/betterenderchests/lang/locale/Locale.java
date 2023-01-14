package me.saif.betterenderchests.lang.locale;

import me.saif.betterenderchests.lang.MessageKey;
import me.saif.betterenderchests.lang.placeholder.PlaceholderResult;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
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
            String[] array = Arrays.stream(stringArray).map(msgLine -> {
                msgLine = ChatColor.translateAlternateColorCodes('&', msgLine);
                return msgLine;
            }).toArray(String[]::new);
            this.messages.put(key, array);
        });
    }

    public String[] getMessage(MessageKey key) {
        return messages.getOrDefault(key, key.getTranslated());
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

    public String getSingleMessage(MessageKey key) {
        return messages.getOrDefault(key, key.getTranslated())[0];
    }

    public String getSingleFormattedMessage(MessageKey key, PlaceholderResult... placeholderResults) {
        String msg = getSingleMessage(key);
        for (PlaceholderResult placeholderResult : placeholderResults) {
            msg = placeholderResult.replace(msg);
        }

        return msg;
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

    @Override
    public String toString() {
        return "Locale{" +
                "lang='" + lang + '\'' +
                ", countries=" + countries +
                ", messages=" + messages.values().stream().map(Arrays::toString).collect(Collectors.toList()) +
                '}';
    }
}
