package com.kbhc.codetest.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;

public class DateUtils {
    public static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            // 1. "2024-11-14T21:20:00+0000" 형태 (ISO 방식)
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"))
            // 2. "2024-11-15 00:10:00" 형태 (공백 방식)
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .toFormatter();

    public static OffsetDateTime parseToOffsetDateTime(String dateStr) {
        TemporalAccessor accessor = FLEXIBLE_FORMATTER.parseBest(dateStr,
                OffsetDateTime::from, LocalDateTime::from);

        if (accessor instanceof OffsetDateTime) {
            return (OffsetDateTime) accessor;
        } else {
            // 타임존이 없는 LocalDateTime인 경우 UTC를 강제로 입힘
            return ((LocalDateTime) accessor).atOffset(ZoneOffset.UTC);
        }
    }
}
