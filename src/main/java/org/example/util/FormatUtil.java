package org.example.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FormatUtil {
    private FormatUtil() {}

    private static final DateTimeFormatter UI_DATE_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public static String format(Instant instant) {
        if(instant == null) return "";
        return UI_DATE_TIME.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    public static String shortId(UUID id) {
        if(id == null) return "";
        String idString = id.toString();
        return idString.substring(idString.length() - 5);
    }

    public static String truncate(String value, int max) {
        if(value == null) return "";
        return value.length() <= max
                ? value
                : value.substring(0, max) + "...";
    }
}
