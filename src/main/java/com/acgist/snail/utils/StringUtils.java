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
	
	/**
	 * 转为LONG
	 */
	public static final long toLong(String value) {
		if(isNumeric(value)) {
			return Long.valueOf(value);
		}
		return 0L;
	}

	/**
	 * 16进制字符串编码
	 */
	public static final String hex(byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		for (int index = 0; index < bytes.length; index++) {
			String hex = Integer.toHexString(bytes[index] & 0xFF);
			if (hex.length() < 2) {
				builder.append(0);
			}
			builder.append(hex);
		}
		return builder.toString();
	}

	/**
	 * 16进制字符串解码
	 */
	public static byte[] unhex(String text) {
		if(text == null) {
			return null;
		}
		int length = text.length();
		byte[] result;
		if (length % 2 == 1) { // 奇数
			length++;
			result = new byte[(length / 2)];
			text = "0" + text;
		} else { // 偶数
			result = new byte[(length / 2)];
		}
		int jndex = 0;
		for (int index = 0; index < length; index += 2) {
			result[jndex] = (byte) Integer.parseInt(text.substring(index, index + 2), 16);
			jndex++;
		}
		return result;
	}
	
}
