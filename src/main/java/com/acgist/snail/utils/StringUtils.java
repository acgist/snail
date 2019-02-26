package com.acgist.snail.utils;

/**
 * 字符串工具
 */
public class StringUtils {

	private static final String NUMERIC_REGEX = "[0-9]+";
	
	/**
	 * 否空字符串
	 */
	public static final boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	/**
	 * 非空字符串
	 */
	public static final boolean isNotEmpty(String value) {
		return !isEmpty(value);
	}
	
	/**
	 * 数字字符串
	 */
	public static final boolean isNumeric(String value) {
		return value != null && value.matches(NUMERIC_REGEX);
	}

	/**
	 * 字符串开始
	 */
	public static final boolean startsWith(String value, String prefix) {
		return value != null && prefix != null && value.startsWith(prefix);
	}
	
}
