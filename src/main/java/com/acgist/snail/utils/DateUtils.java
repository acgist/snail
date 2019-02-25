package com.acgist.snail.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具
 */
public class DateUtils {

	/**
	 * 日期格式化
	 */
	public static final String format(Date date, String pattern) {
		SimpleDateFormat formater = new SimpleDateFormat(pattern);
		return formater.format(date);
	}
	
}
