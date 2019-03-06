package com.acgist.snail.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * utils - 日期
 */
public class DateUtils {
	
	private static final long ONE_MINUTE = 60L;
	private static final long ONE_HOUR = ONE_MINUTE * 60;
	private static final long ONE_DAY = ONE_HOUR * 24;

	/**
	 * 日期格式化
	 */
	public static final String format(Date date, String pattern) {
		SimpleDateFormat formater = new SimpleDateFormat(pattern);
		return formater.format(date);
	}
	
	/**
	 * 时间格式化：保留两个时间单位
	 * @param second 秒
	 */
	public static final String formatSecond(long value) {
		StringBuilder builder = new StringBuilder();
		long day = value / ONE_DAY;
		if(day != 0) {
			builder.append(day).append("天");
			value = value - day * ONE_DAY;
		}
		long hour = value / ONE_HOUR;
		if(hour != 0) {
			builder.append(hour).append("小时");
			value = value - hour * ONE_HOUR;
			if(day != 0) {
				return builder.toString();
			}
		}
		long minute = value / ONE_MINUTE;
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
	
}
