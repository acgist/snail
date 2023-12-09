package com.acgist.snail.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.acgist.snail.config.SystemConfig;

/**
 * 时间工具
 * 
 * @author acgist
 */
public final class DateUtils {

    private DateUtils() {
    }
    
    /**
     * 默认时间格式：{@value}
     */
    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 时间格式工具
     * 
     * @see #DEFAULT_PATTERN
     */
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);
    /**
     * Windows系统时间和Java系统时间相差毫秒数：{@value}
     * Java时间戳转Windows时间戳：System.currentTimeMillis() + {@value}
     * Windows开始时间：1601年01月01日
     * Windows北京开始时间（东八区）：1601-01-01 08:00:00
     */
    private static final long WINDOWS_JAVA_DIFF_TIMEMILLIS = 11644473600000L;
    /**
     * Java和Unix时间戳倍数：{@value}
     */
    private static final int UNIX_TIMESTAMP_SCALE = 1000;
    /**
     * Java和Windows时间戳倍数：{@value}
     */
    private static final int WINDOWS_TIMESTAMP_SCALE = 10_000;
    
    /**
     * 格式化时间
     * 保留两个时间单位
     * 
     * @param seconds 时间（秒）
     * 
     * @return XX天XX小时、XX小时XX分钟、XX分钟XX秒
     */
    public static final String format(long seconds) {
        final TimeUnit secondUnit = TimeUnit.SECONDS;
        final StringBuilder builder = new StringBuilder();
        final long days = secondUnit.toDays(seconds);
        if(days != 0) {
            builder.append(days).append("天");
            seconds = seconds - TimeUnit.DAYS.toSeconds(days);
        }
        final long hours = secondUnit.toHours(seconds);
        if(hours != 0) {
            builder.append(hours).append("小时");
            if(days != 0) {
                return builder.toString();
            }
            seconds = seconds - TimeUnit.HOURS.toSeconds(hours);
        }
        final long minutes = secondUnit.toMinutes(seconds);
        if(minutes != 0) {
            builder.append(minutes).append("分钟");
            if(hours != 0) {
                return builder.toString();
            }
            seconds = seconds - TimeUnit.MINUTES.toSeconds(minutes);
        }
        builder.append(seconds).append("秒");
        return builder.toString();
    }
    
    /**
     * 格式化时间
     * 默认格式：{@value #DEFAULT_PATTERN}
     * 
     * @param date 时间
     * 
     * @return 格式化字符串
     * 
     * @see #dateFormat(Date, String)
     */
    public static final String dateFormat(Date date) {
        return DateUtils.dateFormat(date, DEFAULT_PATTERN);
    }
    
    /**
     * 格式化时间
     * 时间格式为空默认：{@value #DEFAULT_PATTERN}
     * 
     * @param date    时间
     * @param pattern 格式
     * 
     * @return 格式化字符串
     * 
     * @see #localDateTimeFormat(LocalDateTime, String)
     */
    public static final String dateFormat(Date date, String pattern) {
        if(date == null) {
            return null;
        }
        return DateUtils.localDateTimeFormat(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()), pattern);
    }
    
    /**
     * 格式化时间
     * 默认格式：{@value #DEFAULT_PATTERN}
     * 
     * @param localDateTime 时间
     * 
     * @return 格式化字符串
     * 
     * @see #localDateTimeFormat(LocalDateTime, String)
     */
    public static final String localDateTimeFormat(LocalDateTime localDateTime) {
        return DateUtils.localDateTimeFormat(localDateTime, DEFAULT_PATTERN);
    }
    
    /**
     * 格式化时间
     * 时间格式为空默认：{@value #DEFAULT_PATTERN}
     * 
     * @param localDateTime 时间
     * @param pattern       格式
     * 
     * @return 格式化字符串
     */
    public static final String localDateTimeFormat(LocalDateTime localDateTime, String pattern) {
        if(localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter;
        if(DEFAULT_PATTERN.equals(pattern) || pattern == null) {
            formatter = DEFAULT_FORMATTER;
        } else {
            formatter = DateTimeFormatter.ofPattern(pattern);
        }
        return formatter.format(localDateTime);
    }
    
    /**
     * @return Unix时间戳
     * 
     * @see #unixTimestamp(long)
     */
    public static final long unixTimestamp() {
        return DateUtils.unixTimestamp(System.currentTimeMillis());
    }
    
    /**
     * @param timestamp Java时间戳
     * 
     * @return Unix时间戳
     */
    public static final long unixTimestamp(long timestamp) {
        return timestamp / UNIX_TIMESTAMP_SCALE;
    }

    /**
     * Unix时间戳转Java时间戳
     * 
     * @param unixTimestamp Unix时间戳
     * 
     * @return Java时间戳
     */
    public static final long unixToJavaTimestamp(long unixTimestamp) {
        return unixTimestamp * UNIX_TIMESTAMP_SCALE;
    }
    
    /**
     * Unix时间戳转Java时间
     * 
     * @param unixTimestamp Unix时间戳
     * 
     * @return Java时间
     * 
     * @see #unixToJavaTimestamp(long)
     */
    public static final Date unixToJavaDate(long unixTimestamp) {
        return new Date(DateUtils.unixToJavaTimestamp(unixTimestamp));
    }
    
    /**
     * @return Windows时间戳
     * 
     * @see #windowsTimestamp(long)
     */
    public static final long windowsTimestamp() {
        return DateUtils.windowsTimestamp(System.currentTimeMillis());
    }
    
    /**
     * @param timestamp Java时间戳
     * 
     * @return Windows时间戳
     */
    public static final long windowsTimestamp(long timestamp) {
        return (timestamp + WINDOWS_JAVA_DIFF_TIMEMILLIS) * WINDOWS_TIMESTAMP_SCALE;
    }
    
    /**
     * Windows时间戳转Java时间戳
     * 
     * @param windowsTimestamp Windows时间戳
     * 
     * @return Java时间戳
     */
    public static final long windowsToJavaTimestamp(long windowsTimestamp) {
        return windowsTimestamp / WINDOWS_TIMESTAMP_SCALE - WINDOWS_JAVA_DIFF_TIMEMILLIS;
    }
    
    /**
     * Windows时间戳转Java时间
     * 
     * @param windowsTimestamp Windows时间戳
     * 
     * @return Java时间
     * 
     * @see #windowsToJavaTimestamp(long)
     */
    public static final Date windowsToJavaDate(long windowsTimestamp) {
        return new Date(DateUtils.windowsToJavaTimestamp(windowsTimestamp));
    }
    
    /**
     * @return 微秒时间戳
     */
    public static final int timestampUs() {
        return (int) (System.nanoTime() / SystemConfig.DATE_SCALE);
    }
    
}
