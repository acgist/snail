package com.acgist.snail.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>时间工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DateUtils {

	/**
	 * <p>默认时间格式：{@value}</p>
	 */
	public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	/**
	 * <p>Unix和Java时间戳倍数：{@value}</p>
	 */
	private static final int UNIX_JAVA_TIMESTAMP_SCALE = 1000;
	/**
	 * <p>Windows系统时间和Java系统时间相差毫秒数：{@value}</p>
	 * <p>Java时间戳转Windows时间戳：{@value} + {@code System.currentTimeMillis()}</p>
	 */
	private static final long WINDOWS_JAVA_DIFF_TIMEMILLIS = 11644473600000L;
	/**
	 * <p>Windows开始时间（北京时间）</p>
	 * <p>开始时间：（1601年1月1日）北京时间（东八区）</p>
	 */
	private static final LocalDateTime WINDOWS_BEIJIN_BEGIN_TIME = LocalDateTime.of(1601, 01, 01, 8, 00, 00);
	/**
	 * <p>Java和Windows时间戳倍数：{@value}</p>
	 */
	private static final int JAVA_WINDOWS_TIMESTAMP_SCALE = 10_000;
	/**
	 * <p>一秒钟（毫秒）：{@value}</p>
	 */
	public static final long ONE_SECOND = 1000L;
	/**
	 * <p>一分钟（秒数）：{@value}</p>
	 */
	private static final long ONE_MINUTE = 60L;
	/**
	 * <p>一小时（秒数）：{@value}</p>
	 */
	private static final long ONE_HOUR = ONE_MINUTE * 60;
	/**
	 * <p>一天（秒数）：{@value}</p>
	 */
	private static final long ONE_DAY = ONE_HOUR * 24;
	
	/**
	 * <p>时间格式化：保留两个时间单位</p>
	 * 
	 * @param value 时间（单位：秒）
	 * 
	 * @return
	 * <dl>
	 * 	<dt>格式化字符串</dt>
	 * 	<dd>XX天XX小时</dd>
	 * 	<dd>XX小时XX分钟</dd>
	 * 	<dd>XX分钟XX秒</dd>
	 * </dl>
	 */
	public static final String format(long value) {
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
	 * <p>时间格式化</p>
	 * <p>默认格式：{@value #DEFAULT_PATTERN}</p>
	 * 
	 * @param date 时间
	 * 
	 * @see {@link #dateToString(Date, String)}
	 */
	public static final String dateToString(Date date) {
		return dateToString(date, DEFAULT_PATTERN);
	}
	
	/**
	 * <p>时间格式化</p>
	 * 
	 * @param date 时间
	 * @param pattern 格式
	 * 
	 * @return 格式化字符串
	 */
	public static final String dateToString(Date date, String pattern) {
		if(date == null) {
			return null;
		}
		final SimpleDateFormat formater = new SimpleDateFormat(pattern);
		return formater.format(date);
	}
	
	/**
	 * <p>Java时间戳</p>
	 * 
	 * @return Java时间戳
	 */
	public static final long javaTimestamp() {
		return System.currentTimeMillis();
	}

	/**
	 * <p>Java时间戳转Unix时间戳</p>
	 * 
	 * @param javaTimestamp Java时间戳
	 * 
	 * @return Unix时间戳
	 */
	public static final long javaToUnixTimestamp(long javaTimestamp) {
		return javaTimestamp / UNIX_JAVA_TIMESTAMP_SCALE;
	}
	
	/**
	 * <p>Unix时间戳</p>
	 * 
	 * @return Unix时间戳
	 */
	public static final long unixTimestamp() {
		return javaToUnixTimestamp(javaTimestamp());
	}

	/**
	 * <p>Unix时间戳转Java时间戳</p>
	 * 
	 * @param unixTimestamp Unix时间戳
	 * 
	 * @return Java时间戳
	 */
	public static final long unixToJavaTimestamp(long unixTimestamp) {
		return unixTimestamp * UNIX_JAVA_TIMESTAMP_SCALE;
	}
	
	/**
	 * <p>Unix时间戳转Java时间</p>
	 * 
	 * @param unixTimestamp Unix时间戳
	 * 
	 * @return Java时间
	 */
	public static final Date unixToJavaDate(long unixTimestamp) {
		return new Date(unixToJavaTimestamp(unixTimestamp));
	}
	
	/**
	 * <p>时间戳（微秒）</p>
	 * 
	 * @return 时间戳（微秒）
	 */
	public static final int timestampUs() {
		return (int) (System.nanoTime() / 1000);
	}

	/**
	 * <p>Windows时间戳</p>
	 * 
	 * @return Windows时间戳
	 */
	public static final long windowsTimestamp() {
		return (WINDOWS_JAVA_DIFF_TIMEMILLIS + System.currentTimeMillis()) * JAVA_WINDOWS_TIMESTAMP_SCALE;
	}
	
	/**
	 * <p>Windows时间戳（扩展）</p>
	 * <p>使用时间转换</p>
	 * 
	 * @return Windows时间戳
	 */
	public static final long windowsTimestampEx() {
		return DateUtils.diff(WINDOWS_BEIJIN_BEGIN_TIME, LocalDateTime.now()).toMillis() * JAVA_WINDOWS_TIMESTAMP_SCALE;
	}
	
	/**
	 * <p>计算时间差</p>
	 * 
	 * @param begin 开始时间
	 * @param end 结束时间
	 * 
	 * @return 时间差
	 */
	public static final Duration diff(LocalDateTime begin, LocalDateTime end) {
		return Duration.between(begin, end);
	}
	
}
