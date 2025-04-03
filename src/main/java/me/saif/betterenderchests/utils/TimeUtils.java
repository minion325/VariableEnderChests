package me.saif.betterenderchests.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm:ss");

    public static String getCurrentFormattedTime() {
        return formatter.format(LocalDateTime.now());
    }

    public static DateTimeFormatter getFormatter() {
        return formatter;
    }
}
