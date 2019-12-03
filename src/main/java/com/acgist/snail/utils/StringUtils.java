package com.acgist.snail.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;

/**
 * <p>字符串工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class StringUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);
	
	/**
	 * <p>整数正则表达式（包含正负）</p>
	 */
	private static final String NUMERIC_REGEX = "\\-?[0-9]+";
	
	/**
	 * <p>小数、整数正则表达式（包含正负）</p>
	 */
	private static final String DECIMAL_REGEX = "\\-?[0-9]+(\\.[0-9]+)?";
	
	/**
	 * <p>字符串是否为空</p>
	 */
	public static final boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	/**
	 * <p>字符串是否不为空</p>
	 */
	public static final boolean isNotEmpty(String value) {
		return !isEmpty(value);
	}
	
	/**
	 * <p>是否是数字（整数）：{@value #NUMERIC_REGEX}</p>
	 */
	public static final boolean isNumeric(String value) {
		return StringUtils.regex(value, NUMERIC_REGEX, true);
	}

	/**
	 * <p>是否是数字（整数、小数）</p>
	 */
	public static final boolean isDecimal(String value) {
		return StringUtils.regex(value, DECIMAL_REGEX, true);
	}
	
	/**
	 * <p>字符串是否以前缀开始</p>
	 * 
	 * @param value 字符串
	 * @param prefix 前缀
	 * 
	 * @return true-是；false-不是；
	 */
	public static final boolean startsWith(String value, String prefix) {
		return value != null && prefix != null && value.startsWith(prefix);
	}
	
	/**
	 * <p>字符串是否以后缀结束</p>
	 * 
	 * @param value 字符串
	 * @param suffix 后缀
	 * 
	 * @return true-是；false-不是；
	 */
	public static final boolean endsWith(String value, String suffix) {
		return value != null && suffix != null && value.endsWith(suffix);
	}
	
	/**
	 * <p>字节数组转为十六进制字符串</p>
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
	 * <p>十六进制字符串转为字节数组</p>
	 */
	public static final byte[] unhex(String text) {
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
	 * <p>SHA-1散列计算</p>
	 */
	public static final byte[] sha1(byte[] bytes) {
		final MessageDigest digest = DigestUtils.sha1();
		digest.update(bytes);
		return digest.digest();
	}
	
	/**
	 * <p>SHA-1散列计算并转为十六进制字符串</p>
	 */
	public static final String sha1Hex(byte[] bytes) {
		return StringUtils.hex(sha1(bytes));
	}
	
	/**
	 * <p>字符串解码</p>
	 * <p>将经过编码的字符串解码为系统默认编码字符串</p>
	 * 
	 * @param value 原始字符串
	 * @param charset 原始编码
	 * 
	 * @return 字符串（系统默认编码）
	 */
	public static final String charset(String value, String charset) {
		if(StringUtils.isEmpty(value) || StringUtils.isEmpty(charset)) {
			return value;
		}
		try {
			return new String(value.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("字符串解码异常：{}-{}", charset, value, e);
		}
		return value;
	}

	/**
	 * <p>正则表达式验证</p>
	 * 
	 * @param value 字符串
	 * @param regex 正则表达式
	 * @param ignoreCase 是否忽略大小写
	 * 
	 * @return true：匹配；false：不匹配；
	 */
	public static final boolean regex(String value, String regex, boolean ignoreCase) {
		if(value == null || regex == null) {
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
	 * <p>字符串是否相等</p>
	 * 
	 * @param source 原始字符串
	 * @param target 目标字符串
	 * 
	 * @return true-相等；false-不相等；
	 */
	public static final boolean equals(String source, String target) {
		if(source == null) {
			return target == null;
		} else {
			return source.equals(target);
		}
	}
	
	/**
	 * <p>字符串是否相等：忽略大小写</p>
	 */
	public static final boolean equalsIgnoreCase(String source, String target) {
		if(source == null) {
			return target == null;
		} else {
			return source.equalsIgnoreCase(target);
		}
	}

	/**
	 * <p>转换Unicode字符串</p>
	 */
	public static final String toUnicode(String content) {
		char value;
		final StringBuilder builder = new StringBuilder();
		for (int index = 0; index < content.length(); index++) {
			value = content.charAt(index);
			builder.append("\\u");
			if(value <= 0xFF) {
				builder.append("00");
			}
			builder.append(Integer.toHexString(value));
		}
		return builder.toString();
	}
	
	/**
	 * <p>读取Unicode字符串</p>
	 */
	public static final String ofUnicode(String unicode) {
		final String[] hex = unicode.split("\\\\u");
		final StringBuilder builder = new StringBuilder();
		for (int index = 1; index < hex.length; index++) {
			builder.append((char) Integer.parseInt(hex[index], 16));
		}
		return builder.toString();
	}
	
	/**
	 * <p>ByteBuffer转为字符串</p>
	 * <p>默认编码：{@link SystemConfig#DEFAULT_CHARSET}</p>
	 * 
	 * @see {@link #ofByteBuffer(ByteBuffer, String)}
	 */
	public static final String ofByteBuffer(ByteBuffer buffer) {
		return ofByteBuffer(buffer, SystemConfig.DEFAULT_CHARSET);
	}
	
	/**
	 * <p>ByteBuffer转为字符串</p>
	 * 
	 * @param buffer 字节缓冲区
	 * @param charset 编码
	 * 
	 * @return 字符串
	 */
	public static final String ofByteBuffer(ByteBuffer buffer, String charset) {
		if(charset == null) {
			charset = SystemConfig.DEFAULT_CHARSET;
		}
		String content = null;
		final CharsetDecoder decoder = Charset.forName(charset).newDecoder();
		decoder.onMalformedInput(CodingErrorAction.IGNORE);
		try {
			if(buffer.position() != 0) {
				buffer.flip();
			}
			content = decoder.decode(buffer).toString();
			buffer.compact();
		} catch (CharacterCodingException e) {
			LOGGER.error("ByteBuffer转为字符串异常", e);
		}
		return content;
	}
	
	/**
	 * <p>输入流转为字符串</p>
	 * 
	 * @param input 输入流
	 * @param charset 编码
	 * 
	 * @return 字符串
	 */
	public static final String ofInputStream(InputStream input, String charset) {
		if(input == null) {
			return null;
		}
		if(charset == null) {
			charset = SystemConfig.DEFAULT_CHARSET;
		}
		int index;
		final char[] chars = new char[1024];
		final StringBuilder builder = new StringBuilder();
		try {
			final var reader = new InputStreamReader(input, charset);
			while((index = reader.read(chars)) != -1) {
				builder.append(new String(chars, 0, index));
			}
		} catch (IOException e) {
			LOGGER.error("输入流转为字符串异常", e);
		}
		return builder.toString();
	}
	
}
