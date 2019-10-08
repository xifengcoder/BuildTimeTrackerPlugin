package com.yxf.buildtimetracker

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class DateUtils {
    public DateUtils() {}

    long getLocalMidnightUTCTimestamp() {
        DateTime.now().withTime(0, 0, 0, 0).withZone(DateTimeZone.UTC).getMillis()
    }
}
