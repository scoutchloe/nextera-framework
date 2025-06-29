package com.nextera.managenextera.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Scout
 * @date 2025-06-29 19:49
 * @since 1.0
 */
public final class LocalTimeUtil {

    public static String localDateTimeToString(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return localDateTime.format(formatter);
    }
}