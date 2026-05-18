package com.io.begstd.slot.model.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class LocalDateTimes {

    public static LocalDateTime from(Date date) {
        // Asserts.notNull(date, "Date must not be null");
        return fromNullable(date);
    }

    public static LocalDateTime fromNullable(Date date, LocalDateTime defaultValue) {
        LocalDateTime t = fromNullable(date);
        if (t == null) {
            return defaultValue;
        }

        return t;
    }

    public static LocalDateTime fromNullable(Date date) {
        return localDateTimeFrom(date);
    }

    public static LocalDateTime localDateTimeFrom(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.from(date.toInstant().atZone(ZoneId.systemDefault()));
    }
}
