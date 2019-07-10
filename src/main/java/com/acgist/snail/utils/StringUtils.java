package com.acgist.snail.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>字符串工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class StringUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);
	
	/**
	 * 正负整数
	 */
	private static final String NUMERIC_REGEX = "\\-?[0-9]+";
	
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
	 * 数字字符串：正负整数。
	 */
	public static final boolean isNumeric(String value) {
		return StringUtils.regex(value, NUMERIC_REGEX, true);
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
	 * 转为Long
	 */
	public static final long toLong(String value) {
		if(isNumeric(value)) {
			return Long.valueOf(value);
		}
		return 0L;
	}

	/**
	 * 字符数组转为十六进制字符串。
	 */
	public static final String hex(byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		String hex;
		final StringBuilder builder = new StringBuilder();
		for (int index = 0; index < bytes.length; index++) {
			hex = Integer.toHexString(bytes[index] & 0xFF);
			if (hex.length() < 2) {
				builder.append(0);
			}
			builder.append(hex);
		}
		return builder.toString().toLowerCase();
	}

	/**
	 * 十六进制字符串转为字符数组。
	 */
	public static byte[] unhex(String text) {
		if(text == null) {
			return null;
		}
		byte[] result;
		int length = text.length();
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
	 * SHA1散列计算。
	 */
	public static final byte[] sha1(byte[] bytes) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA1");
			digest.update(bytes);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("SHA1计算异常", e);
		}
		return null;
	}
	
	/**
	 * SHA1散列计算并转为十六进制字符串。
	 */
	public static final String sha1Hex(byte[] bytes) {
		return StringUtils.hex(sha1(bytes));
	}
	
	/**
	 * 字符串转码。
	 * 
	 * @param value 原始字符串
	 * @param charset 原始编码格式
	 * 
	 * @return 系统默认编码的字符串
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

	/**
	 * 正则表达式验证
	 * 
	 * @param value 字符串
	 * @param regex 正则表达式
	 * @param ignoreCase 忽略大小写
	 */
	public static final boolean regex(String value, String regex, boolean ignoreCase) {
		if(value == null) {
			return false;
		}
		Pattern pattern;
		if(ignoreCase) {
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		} else {
			pattern = Pattern.compile(regex);
		}
		final Matcher matcher = pattern.matcher(value);
		return matcher.matches();
	}
	
	/**
	 * 判断字符串是否相等
	 */
	public static final boolean equals(String source, String target) {
		if(source == null) {
			return target == null;
		} else {
			return source.equals(target);
		}
	}

	/**
	 * 转换Unicode
	 */
	public static final String toUnicode(String content) {
		char value;
		final StringBuilder unicode = new StringBuilder();
		for (int index = 0; index < content.length(); index++) {
			value = content.charAt(index);
			unicode.append("\\u");
			if(value <= 0xFF) {
				unicode.append("00");
			}
			unicode.append(Integer.toHexString(value));
		}
		return unicode.toString();
	}
	
	/**
	 * 读取Unicode
	 */
	public static final String fromUnicode(String unicode) {
		int value;
		final String[] hex = unicode.split("\\\\u");
		final StringBuffer content = new StringBuffer();
		for (int index = 1; index < hex.length; index++) {
			value = Integer.parseInt(hex[index], 16);
			content.append((char) value);
		}
		return content.toString();
	}
	
}
