package com.acgist.snail.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>日期工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DateUtils {

	/**
	 * 默认时间格式
	 */
	public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	/**
	 * UNIX和JAVA时间戳倍数
	 */
	private static final int UNIX_JAVA_TIMESTAMP_SCALE = 1000;
	/**
	 * Window系统时间和JAVA系统时间相差毫秒数
	 */
	private static final long WINDOW_JAVA_DIFF_TIMEMILLIS = 11644473600000L;
	/**
	 * <p>Window开始时间戳（北京时间）</p>
	 * <p>开始时间（1601年1月1日）北京时间（东八区）</p>
	 * <p>转换Java时间戳：11644473600000L + System.currentTimeMillis()</p>
	 */
	private static final LocalDateTime WINDOW_BEIJIN_BEGIN_TIME = LocalDateTime.of(1601, 01, 01, 8, 00, 00);
	/**
	 * 一秒钟（毫秒）
	 */
	public static final long ONE_SECOND = 1000L;
	/**
	 * 一分钟（秒数）
	 */
	private static final long ONE_MINUTE = 60L;
	/**
	 * 一小时（秒数）
	 */
	private static final long ONE_HOUR = ONE_MINUTE * 60;
	/**
	 * 一天（秒数）
	 */
	private static final long ONE_DAY = ONE_HOUR * 24;
	
	/**
	 * <p>时间格式化：保留两个时间单位</p>
	 * 
	 * @param second 时间（单位：秒）
	 * 
	 * @return
	 * <dl>
	 * 	<dt>格式化后字符串：</dt>
	 * 	<dd>XX天XX小时</dd>
	 * 	<dd>XX小时XX分钟</dd>
	 * 	<dd>XX分钟XX秒</dd>
	 * </dl>
	 */
	public static final String formatSecond(long value) {
		final StringBuilder builder = new StringBuilder();
		final long day = value / ONE_DAY;
		if(day != 0) {
			builder.append(day).append("天");
			value = value - day * ONE_DAY;
		}
		final long hour = value / ONE_HOUR;
		if(hour != 0) {
			builder.append(hour).append("小时");
			value = value - hour * ONE_HOUR;
			if(day != 0) {
				return builder.toString();
			}
		}
		final long minute = value / ONE_MINUTE;
		if(minute != 0) {
			builder.append(minute).append("分钟");
			value = value - minute * ONE_MINUTE;
			if(hour != 0) {
				return builder.toString();
			}
		}
		builder.append(value).append("秒");
		return builder.toString();
	}
	
	/**
	 * 时间格式化：{@linkplain DateUtils#DEFAULT_PATTERN 默认格式}
	 */
	public static final String dateToString(Date date) {
		return dateToString(date, DEFAULT_PATTERN);
	}
	
	/**
	 * 时间格式化
	 * 
	 * @param date 时间
	 * @param pattern 格式
	 * 
	 * @return 时间字符串
	 */
	public static final String dateToString(Date date, String pattern) {
		if(date == null) {
			return null;
		}
		final SimpleDateFormat formater = new SimpleDateFormat(pattern);
		return formater.format(date);
	}
	
	/**
	 * Java时间戳
	 */
	public static final long javaTimestamp() {
		return System.currentTimeMillis();
	}

	/**
	 * Java时间戳转Unix时间戳
	 * 
	 * @param javaTimestamp Java时间戳
	 * 
	 * @return Unix时间戳
	 */
	public static final long javaToUnixTimestamp(long javaTimestamp) {
		return javaTimestamp / UNIX_JAVA_TIMESTAMP_SCALE;
	}
	
	/**
	 * Unix时间戳
	 */
	public static final long unixTimestamp() {
		return javaToUnixTimestamp(javaTimestamp());
	}

	/**
	 * Unix时间戳转Java时间戳
	 * 
	 * @param unixTimestamp Unix时间戳
	 * 
	 * @return Java时间戳
	 */
	public static final long unixToJavaTimestamp(long unixTimestamp) {
		return unixTimestamp * UNIX_JAVA_TIMESTAMP_SCALE;
	}
	
	/**
	 * Unix时间戳转Java时间
	 * 
	 * @param unixTimestamp Unix时间戳
	 * 
	 * @return Java时间
	 */
	public static final Date unixToJavaDate(long unixTimestamp) {
		return new Date(unixToJavaTimestamp(unixTimestamp));
	}
	
	/**
	 * 时间戳（微秒）
	 */
	public static final int timestampUs() {
		return (int) (System.nanoTime() / 1000);
	}

	/**
	 * <p>Windows时间戳</p>
	 * <p>时间单位：微秒 * 10</p>
	 * <p>使用Java时间戳 + 相差时间戳计算</p>
	 */
	public static final long windowTimestamp() {
		return (WINDOW_JAVA_DIFF_TIMEMILLIS + System.currentTimeMillis()) * 10_000;
	}
	
	/**
	 * <p>Windows时间戳</p>
	 * <p>时间单位：微秒 * 10</p>
	 * <p>使用时间差计算</p>
	 */
	public static final long windowTimestampEx() {
		return DateUtils.diff(WINDOW_BEIJIN_BEGIN_TIME, LocalDateTime.now()).toMillis() * 10_000;
	}
	
	/**
	 * 时间相减
	 * 
	 * @param begin 开始时间
	 * @param end 结束时间
	 * 
	 * @return 相差时间
	 */
	public static final Duration diff(LocalDateTime begin, LocalDateTime end) {
		return Duration.between(begin, end);
	}
	
}
