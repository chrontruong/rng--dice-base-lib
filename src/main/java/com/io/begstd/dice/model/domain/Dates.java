package com.io.begstd.dice.model.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public final class Dates {

    /*
     * 2019-01-14T03:58:20Z - ISO_OFFSET_DATETIME 2019-01-14T03:58:20+00:00 -
     * ISO_OFFSET_DATETIME 2019-01-14T03:58:20+0000 - yyyy-MM-dd'T'HH:mm:ssXX
     */
    public static Date fromISO8601(String datetime) {
        // Asserts.notNull(datetime, "Datetime must not be null");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXX"); // parse zone have
                                                                                                      // no ':'

        try {
            return from(datetime, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return from(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    public static Date from(String datetime, DateTimeFormatter formatter) {
        // Asserts.notNull(datetime, "Datetime must not be null");
        return Date.from(Instant.from(formatter.parse(datetime)));
    }

    public static Date from(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }

        return Date.from(zonedDateTime.toInstant());
    }

    public static Date from(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return from(localDateTime, ZoneId.systemDefault());
    }

    public static Date from(LocalDateTime localDateTime, ZoneId zoneId) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(zoneId).toInstant());
    }

}
