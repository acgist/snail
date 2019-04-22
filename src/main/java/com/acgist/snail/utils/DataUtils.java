package com.acgist.snail.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DataUtils {

	private static final int UNIX_JAVA_TIMESTAMP_SCALE = 1000;
	private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * 时间格式化，格式：{@linkplain DataUtils#DEFAULT_PATTERN yyyy-MM-dd HH:mm:ss}
	 */
	public static final String dateToString(Date date) {
		return dateToString(date, DEFAULT_PATTERN);
	}
	
	public static final String dateToString(Date date, String pattern) {
		if(date == null) {
			return null;
		}
		final SimpleDateFormat formater = new SimpleDateFormat(pattern);
		return formater.format(date);
	}
	
	public static final long unixTimestamp() {
		return javaToUnixTimestamp(javaTimestamp());
	}
	
	public static final long javaToUnixTimestamp(long javaTimestamp) {
		return javaTimestamp / UNIX_JAVA_TIMESTAMP_SCALE;
	}
	
	public static final long javaTimestamp() {
		return System.currentTimeMillis();
	}
	
	public static final long unixToJavaTimestamp(long unixTimestamp) {
		return unixTimestamp * UNIX_JAVA_TIMESTAMP_SCALE;
	}
	
	public static final Date unixToJavaDate(long unixTimestamp) {
		return new Date(unixToJavaTimestamp(unixTimestamp));
	}
	
}
