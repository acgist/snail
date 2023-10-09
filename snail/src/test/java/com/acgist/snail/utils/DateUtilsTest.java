package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Date;

import org.junit.jupiter.api.Test;

class DateUtilsTest extends Performance {

    @Test
    void testFormat() {
        assertEquals("11天13小时", DateUtils.format(1000000));
        assertEquals("1天3小时", DateUtils.format(100000));
        assertEquals("2小时46分钟", DateUtils.format(10000));
        assertEquals("16分钟40秒", DateUtils.format(1000));
        assertEquals("10秒", DateUtils.format(10));
        this.costed(100000, () -> DateUtils.format(1000));
    }

    @Test
    void testDateFormat() {
        this.log(DateUtils.dateFormat(new Date()));
        this.log(DateUtils.localDateTimeFormat(LocalDateTime.now()));
        assertEquals(DateUtils.dateFormat(new Date()), DateUtils.localDateTimeFormat(LocalDateTime.now()));
    }
    
    @Test
    void testTimestamp() {
        final long value = System.currentTimeMillis();
        this.log(DateUtils.unixTimestamp());
        this.log(DateUtils.windowsTimestamp());
        final long unix = DateUtils.unixTimestamp(value);
        final long windows = DateUtils.windowsTimestamp(value);
        this.log(unix);
        this.log(windows);
        final long unixJava = DateUtils.unixToJavaTimestamp(unix);
        final long windowsJava = DateUtils.windowsToJavaTimestamp(windows);
        this.log(unixJava);
        this.log(windowsJava);
        final Date unixJavaDate = DateUtils.unixToJavaDate(unix);
        final Date windowsJavaDate = DateUtils.windowsToJavaDate(windows);
        this.log(unixJavaDate);
        this.log(windowsJavaDate);
        assertEquals(DateUtils.dateFormat(unixJavaDate, "yyyy-MM-dd HH:mm:ss"), DateUtils.dateFormat(windowsJavaDate, "yyyy-MM-dd HH:mm:ss"));
    }
    
    @Test
    void testCosted() {
        assertDoesNotThrow(() -> {
            this.costed(100000, () -> DateUtils.dateFormat(new Date()));
            this.costed(100000, () -> DateUtils.localDateTimeFormat(LocalDateTime.now()));
            this.costed(100000, () -> DateUtils.dateFormat(new Date(), "yyyy-MM-dd"));
            this.costed(100000, () -> DateUtils.localDateTimeFormat(LocalDateTime.now(), "yyyy-MM-dd"));
        });
    }
    
}
