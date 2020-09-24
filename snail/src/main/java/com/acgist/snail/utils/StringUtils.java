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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;

/**
 * <p>字符串工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class StringUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private StringUtils() {
	}
	
	/**
	 * <p>数字正则表达式（正负整数）：{@value}</p>
	 */
	private static final String NUMERIC_REGEX = "\\-?[0-9]+";
	/**
	 * <p>数字正则表达式（正负小数、正负整数）：{@value}</p>
	 */
	private static final String DECIMAL_REGEX = "\\-?[0-9]+(\\.[0-9]+)?";
	/**
	 * <p>参数键值对连接符：{@value}</p>
	 */
	private static final String ARG_SEPARATOR = "=";
	
	/**
	 * <p>字符串是否为空</p>
	 * 
	 * @param value 字符串
	 * 
	 * @return {@code true}-空；{@code false}-非空；
	 */
	public static final boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	/**
	 * <p>字符串是否非空</p>
	 * 
	 * @param value 字符串
	 * 
	 * @return {@code true}-非空；{@code false}-空；
	 */
	public static final boolean isNotEmpty(String value) {
		return !isEmpty(value);
	}
	
	/**
	 * <p>判断{@code value}是不是{@linkplain #NUMERIC_REGEX 数值}</p>
	 * <p>正负整数</p>
	 * 
	 * @param value 字符串
	 * 
	 * @return {@code true}-是；{@code false}-不是；
	 */
	public static final boolean isNumeric(String value) {
		return StringUtils.regex(value, NUMERIC_REGEX, true);
	}

	/**
	 * <p>判断{@code value}是不是{@linkplain #DECIMAL_REGEX 数值}</p>
	 * <p>正负小数、正负整数</p>
	 * 
	 * @param value 字符串
	 * 
	 * @return {@code true}-是；{@code false}-不是；
	 */
	public static final boolean isDecimal(String value) {
		return StringUtils.regex(value, DECIMAL_REGEX, true);
	}
	
	/**
	 * <p>判断{@code value}是不是以{@code prefix}开始</p>
	 * 
	 * @param value 字符串
	 * @param prefix 前缀
	 * 
	 * @return {@code true}-是；{@code false}-不是；
	 */
	public static final boolean startsWith(String value, String prefix) {
		return value != null && prefix != null && value.startsWith(prefix);
	}
	
	/**
	 * <p>判断{@code value}是不是以{@code suffix}结束</p>
	 * 
	 * @param value 字符串
	 * @param suffix 后缀
	 * 
	 * @return {@code true}-是；{@code false}-不是；
	 */
	public static final boolean endsWith(String value, String suffix) {
		return value != null && suffix != null && value.endsWith(suffix);
	}
	
	/**
	 * <p>将字节数组转为十六进制字符串</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return 十六进制字符串
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
	 * <p>将十六进制字符串转为字节数组</p>
	 * 
	 * @param content 十六进制字符串
	 * 
	 * @return 字节数组
	 */
	public static final byte[] unhex(String content) {
		if(content == null) {
			return null;
		}
		byte[] result;
		int length = content.length();
		if (length % 2 == 1) { // 奇数
			length++;
			result = new byte[(length / 2)];
			content = "0" + content;
		} else { // 偶数
			result = new byte[(length / 2)];
		}
		int jndex = 0;
		for (int index = 0; index < length; index += 2) {
			result[jndex] = (byte) Integer.parseInt(content.substring(index, index + 2), 16);
			jndex++;
		}
		return result;
	}

	/**
	 * <p>计算{@code bytes}的SHA-1散列值</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return SHA-1散列值
	 */
	public static final byte[] sha1(byte[] bytes) {
		final MessageDigest digest = DigestUtils.sha1();
		digest.update(bytes);
		return digest.digest();
	}
	
	/**
	 * <p>计算{@code bytes}的SHA-1散列值并转为十六进制字符串</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return 十六进制SHA-1散列值字符串
	 */
	public static final String sha1Hex(byte[] bytes) {
		return StringUtils.hex(sha1(bytes));
	}
	
	/**
	 * <p>字符串编码转换</p>
	 * 
	 * @param value 字符串
	 * @param from 输入编码
	 * 
	 * @return 字符串
	 * 
	 * @see #charset(String, String, String)
	 */
	public static final String charsetFrom(String value, String from) {
		return charset(value, from, null);
	}
	
	/**
	 * <p>字符串编码转换</p>
	 * 
	 * @param value 字符串
	 * @param to 输出编码
	 * 
	 * @return 字符串
	 * 
	 * @see #charset(String, String, String)
	 */
	public static final String charsetTo(String value, String to) {
		return charset(value, null, to);
	}
	
	/**
	 * <p>字符串编码转换</p>
	 * 
	 * @param value 字符串
	 * @param from 输入编码：{@code null}-系统默认编码
	 * @param to 输出编码：{@code null}-系统默认编码
	 * 
	 * @return 字符串
	 */
	public static final String charset(String value, String from, String to) {
		if(StringUtils.isEmpty(value)) {
			return value;
		}
		try {
			if(from == null && to == null) {
				return value;
			} else if(from == null) {
				return new String(value.getBytes(), to);
			} else if(to == null) {
				return new String(value.getBytes(from));
			} else {
				return new String(value.getBytes(from), to);
			}
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("字符串编码转换异常：{}-{}-{}", from, to, value, e);
		}
		return value;
	}

	/**
	 * <p>判断{@code value}是否匹配正则表达式{@code regex}</p>
	 * 
	 * @param value 字符串
	 * @param regex 正则表达式
	 * @param ignoreCase 是否忽略大小写
	 * 
	 * @return {@code true}-匹配；{@code false}-不匹配；
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
	 * <p>判断字符串是否相等</p>
	 * 
	 * @param source 原始字符串
	 * @param target 目标字符串
	 * 
	 * @return {@code true}-相等；{@code false}-不等；
	 */
	public static final boolean equals(String source, String target) {
		if(source == null) {
			return target == null;
		} else {
			return source.equals(target);
		}
	}
	
	/**
	 * <p>判断字符串是否相等（忽略大小写）</p>
	 * 
	 * @param source 原始字符串
	 * @param target 目标字符串
	 * 
	 * @return {@code true}-相等；{@code false}-不等；
	 */
	public static final boolean equalsIgnoreCase(String source, String target) {
		if(source == null) {
			return target == null;
		} else {
			return source.equalsIgnoreCase(target);
		}
	}

	/**
	 * <p>将字符串转换为Unicode字符串</p>
	 * 
	 * @param content 字符串
	 * 
	 * @return Unicode字符串
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
	 * 
	 * @param unicode Unicode字符串
	 * 
	 * @return 字符串
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
	 * <p>将{@code ByteBuffer}转为字符串</p>
	 * <p>默认编码：{@link SystemConfig#DEFAULT_CHARSET}</p>
	 * 
	 * @param buffer 字节缓存
	 * 
	 * @return 字符串
	 * 
	 * @see #ofByteBuffer(ByteBuffer, String)
	 */
	public static final String ofByteBuffer(ByteBuffer buffer) {
		return ofByteBuffer(buffer, SystemConfig.DEFAULT_CHARSET);
	}
	
	/**
	 * <p>将{@code ByteBuffer}转为字符串</p>
	 * 
	 * @param buffer 字节缓冲
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
	 * <p>将输入流转为字符串</p>
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
	
	/**
	 * <p>获取参数值</p>
	 * 
	 * @param arg 参数键值对
	 * @param key 参数键
	 * 
	 * @return 参数值
	 */
	public static final String argValue(final String arg, final String key) {
		String value = arg;
		if(startsWith(value, key)) { // 去掉键
			value = value.substring(key.length()).trim();
			if(startsWith(value, ARG_SEPARATOR)) { // 去掉键值对连接符
				value = value.substring(ARG_SEPARATOR.length()).trim();
				return value;
			}
		}
		return null;
	}
	
	/**
	 * <p>获取文本编码</p>
	 * <p>支持编码：GBK、UTF-8</p>
	 * 
	 * @param content 文本内容
	 * 
	 * @return 编码格式
	 */
	public static final String getCharset(String content) {
		if(StringUtils.isEmpty(content)) {
			return SystemConfig.CHARSET_UTF8;
		}
		// UTF-8
		final var gbkEncoder = Charset.forName(SystemConfig.CHARSET_GBK).newEncoder();
		if(gbkEncoder.canEncode(content)) {
			return SystemConfig.CHARSET_UTF8;
		}
		// GBK
		final String gbkContent = StringUtils.charsetTo(content, SystemConfig.CHARSET_GBK);
		if(gbkEncoder.canEncode(gbkContent)) {
			return SystemConfig.CHARSET_GBK;
		}
		// 默认编码：UTF-8
		return SystemConfig.CHARSET_UTF8;
	}
	
	/**
	 * <p>将对象转为字符串</p>
	 * <p>默认编码：UTF-8</p>
	 * 
	 * @param object 对象
	 * 
	 * @return 字符串
	 */
	public static final String getString(Object object) {
		return getString(object, null);
	}
	
	/**
	 * <p>将对象转为字符串</p>
	 * <p>编码可以为空，默认编码：UTF-8</p>
	 * 
	 * @param object 对象
	 * @param encoding 编码
	 * 
	 * @return 字符串
	 */
	public static final String getString(Object object, String encoding) {
		if(object == null) {
			return null;
		}
		if(object instanceof byte[]) {
			final byte[] bytes = (byte[]) object;
			if(encoding == null) {
				return new String(bytes);
			} else {
				try {
					return new String(bytes, encoding);
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("字符编码异常：{}", encoding, e);
				}
				return new String(bytes);
			}
		} else {
			return object.toString();
		}
	}
	
	/**
	 * <p>将对象转为字符串</p>
	 * <p>自动获取编码，自动转换支持编码：GBK、UTF-8</p>
	 * 
	 * @param object 对象
	 * @param encoding 编码
	 * 
	 * @return 字符串
	 */
	public static final String getStringCharset(Object object, String encoding) {
		if(encoding != null) {
			return getString(object, encoding);
		} else {
			final String objectUtf8 = getString(object, encoding);
			final String charset = getCharset(objectUtf8);
			if(SystemConfig.CHARSET_GBK.equals(charset)) {
				return getString(object, SystemConfig.CHARSET_GBK);
			}
			return objectUtf8;
		}
	}
	
	
	/**
	 * <p>去掉字符串所有空白字符</p>
	 * 
	 * @param content 原始内容
	 * 
	 * @return 去掉空白字符的字符串
	 */
	public static final String trimAllBlank(String content) {
		if(content == null) {
			return content;
		}
		return content.replaceAll("\\s", "");
	}
	
	/**
	 * <p>读取文本每行信息</p>
	 * 
	 * @param content 文本
	 * 
	 * @return 每行列表
	 */
	public static final List<String> readLines(String content) {
		if(content == null) {
			return List.of();
		}
		return Stream.of(content.split(SystemConfig.LINE_SEPARATOR))
			 .map(value -> value.trim())
			 .collect(Collectors.toList());
	}
	
}
