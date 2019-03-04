package com.acgist.snail.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * utils - 字符串
 */
public class StringUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);
	
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
	 * 字符串结束
	 */
	public static final boolean endsWith(String value, String suffix) {
		return value != null && suffix != null && value.endsWith(suffix);
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

	/**
	 * SHA1
	 */
	public static final String sha1(byte[] bytes) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			digest.update(bytes);
			return StringUtils.hex(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("SHA1计算异常", e);
		}
		return null;
	}
	
	/**
	 * 编码
	 */
	public static final String charset(String value, String charset) {
		if(StringUtils.isEmpty(value) || charset == null) {
			return value;
		}
		try {
			return new String(value.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("字符串解码异常：{}", value, e);
		}
		return value;
	}
	
}
