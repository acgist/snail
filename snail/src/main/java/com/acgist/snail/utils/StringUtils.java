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
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;

/**
 * <p>字符串工具</p>
 * 
 * @author acgist
 */
public final class StringUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);
	
	private StringUtils() {
	}
	
	/**
	 * <p>数值正则表达式（正负整数）：{@value}</p>
	 */
	private static final String NUMERIC_REGEX = "\\-?[0-9]+";
	/**
	 * <p>数值正则表达式（正负小数、正负整数）：{@value}</p>
	 */
	private static final String DECIMAL_REGEX = "\\-?[0-9]+(\\.[0-9]+)?";
	/**
	 * <p>空白字符正则表达式</p>
	 */
	private static final String BLANK_REGEX = "\\s";
	
	/**
	 * <p>判断字符串是否为空</p>
	 * 
	 * @param value 字符串
	 * 
	 * @return 是否为空
	 */
	public static final boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	/**
	 * <p>判断字符串是否非空</p>
	 * 
	 * @param value 字符串
	 * 
	 * @return 是否非空
	 */
	public static final boolean isNotEmpty(String value) {
		return !isEmpty(value);
	}
	
	/**
	 * <p>判断字符串是否为数值</p>
	 * 
	 * @param value 字符串
	 * 
	 * @return 是否为数值
	 * 
	 * @see #NUMERIC_REGEX
	 */
	public static final boolean isNumeric(String value) {
		return StringUtils.regex(value, NUMERIC_REGEX, true);
	}

	/**
	 * <p>判断字符串是否为数值</p>
	 * 
	 * @param value 字符串
	 * 
	 * @return 是否为数值
	 * 
	 * @see #DECIMAL_REGEX
	 */
	public static final boolean isDecimal(String value) {
		return StringUtils.regex(value, DECIMAL_REGEX, true);
	}
	
	/**
	 * <p>判断字符串是否以前缀开始</p>
	 * 
	 * @param value 字符串
	 * @param prefix 前缀
	 * 
	 * @return 是否以前缀开始
	 */
	public static final boolean startsWith(String value, String prefix) {
		return value != null && prefix != null && value.startsWith(prefix);
	}
	
	/**
	 * <p>判断字符串是否以前缀开始（忽略大小写）</p>
	 * 
	 * @param value 字符串
	 * @param prefix 前缀
	 * 
	 * @return 是否以前缀开始
	 */
	public static final boolean startsWithIgnoreCase(String value, String prefix) {
		if(Objects.isNull(value) || Objects.isNull(prefix)) {
			return false;
		}
		final int valueLength = value.length();
		final int prefixLength = prefix.length();
		if(valueLength < prefixLength) {
			return false;
		}
		return equalsIgnoreCase(value.substring(0, prefixLength), prefix);
	}
	
	/**
	 * <p>判断字符串是否以后缀结束</p>
	 * 
	 * @param value 字符串
	 * @param suffix 后缀
	 * 
	 * @return 是否以后缀结束
	 */
	public static final boolean endsWith(String value, String suffix) {
		return value != null && suffix != null && value.endsWith(suffix);
	}
	
	/**
	 * <p>判断字符串是否以后缀结束（忽略大小写）</p>
	 * 
	 * @param value 字符串
	 * @param suffix 后缀
	 * 
	 * @return 是否以后缀结束
	 */
	public static final boolean endsWithIgnoreCase(String value, String suffix) {
		if(Objects.isNull(value) || Objects.isNull(suffix)) {
			return false;
		}
		final int valueLength = value.length();
		final int suffixLength = suffix.length();
		if(valueLength < suffixLength) {
			return false;
		}
		return equalsIgnoreCase(value.substring(valueLength - suffixLength), suffix);
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
				builder.append(SymbolConfig.Symbol.ZERO.toString());
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
		// byte十六进制长度
		final int byteHexLength = 2;
		int length = content.length();
		if (length % byteHexLength != 0) {
			// 填充字符
			length++;
			content = SymbolConfig.Symbol.ZERO.toString() + content;
		}
		int jndex = 0;
		final byte[] hexBytes = new byte[length / byteHexLength];
		for (int index = 0; index < length; index += byteHexLength) {
			hexBytes[jndex] = (byte) Integer.parseInt(content.substring(index, index + byteHexLength), 16);
			jndex++;
		}
		return hexBytes;
	}

	/**
	 * <p>计算字节数组的十六进制SHA-1散列值字符串</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return 十六进制SHA-1散列值字符串
	 */
	public static final String sha1Hex(byte[] bytes) {
		return StringUtils.hex(DigestUtils.sha1(bytes));
	}
	
	/**
	 * <p>字符串编码转换</p>
	 * 
	 * @param value 原始字符串
	 * @param from 输入编码
	 * 
	 * @return 目标字符串
	 * 
	 * @see #charset(String, String, String)
	 */
	public static final String charsetFrom(String value, String from) {
		return charset(value, from, null);
	}
	
	/**
	 * <p>字符串编码转换</p>
	 * 
	 * @param value 原始字符串
	 * @param to 输出编码
	 * 
	 * @return 目标字符串
	 * 
	 * @see #charset(String, String, String)
	 */
	public static final String charsetTo(String value, String to) {
		return charset(value, null, to);
	}
	
	/**
	 * <p>字符串编码转换</p>
	 * 
	 * @param value 原始字符串
	 * @param from 输入编码
	 * @param to 输出编码
	 * 
	 * @return 目标字符串
	 */
	public static final String charset(String value, String from, String to) {
		if(isEmpty(value)) {
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
			LOGGER.error("字符串编码转换异常：{}-{}-{}", value, from, to, e);
		}
		return value;
	}

	/**
	 * <p>判断字符串是否匹配正则表达式</p>
	 * 
	 * @param value 字符串
	 * @param regex 正则表达式
	 * @param ignoreCase 是否忽略大小写
	 * 
	 * @return 是否匹配正则表达式
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
		return pattern.matcher(value).matches();
	}
	
	/**
	 * <p>判断字符串是否相等</p>
	 * 
	 * @param source 原始字符串
	 * @param target 目标字符串
	 * 
	 * @return 是否相等
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
	 * @return 是否相等
	 */
	public static final boolean equalsIgnoreCase(String source, String target) {
		if(source == null) {
			return target == null;
		} else {
			return source.equalsIgnoreCase(target);
		}
	}
	
	/**
	 * <p>字符串转为对应编码的字节数组</p>
	 * 
	 * @param message 字符串
	 * @param charset 编码
	 * 
	 * @return 字节数组
	 */
	public static final byte[] toBytes(String message, String charset) {
		if (charset == null) {
			return message.getBytes();
		} else {
			try {
				return message.getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("字符编码异常:{}-{}", charset, message, e);
			}
			return message.getBytes();
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
		int length;
		char value;
		String hex;
		final int charHexLength = Integer.BYTES;
		final StringBuilder builder = new StringBuilder();
		for (int index = 0; index < content.length(); index++) {
			builder.append("\\u");
			value = content.charAt(index);
			hex = Integer.toHexString(value);
			length = hex.length();
			if(length < charHexLength) {
				builder.append(SymbolConfig.Symbol.ZERO.toString().repeat(charHexLength - length));
			}
			builder.append(Integer.toHexString(value));
		}
		return builder.toString();
	}
	
	/**
	 * <p>将Unicode字符串转为字符串</p>
	 * 
	 * @param unicode Unicode字符串
	 * 
	 * @return 字符串
	 */
	public static final String ofUnicode(String unicode) {
		final String[] hex = unicode.split("\\\\u");
		final StringBuilder builder = new StringBuilder();
		for (int index = 1; index < hex.length; index++) {
			// 去掉首个空白字符
			builder.append((char) Integer.parseInt(hex[index], Character.SIZE));
		}
		return builder.toString();
	}
	
	/**
	 * <p>将ByteBuffer转为字符串</p>
	 * <p>默认编码：{@link SystemConfig#DEFAULT_CHARSET}</p>
	 * 
	 * @param buffer ByteBuffer
	 * 
	 * @return 字符串
	 * 
	 * @see #ofByteBuffer(ByteBuffer, String)
	 */
	public static final String ofByteBuffer(ByteBuffer buffer) {
		return ofByteBuffer(buffer, SystemConfig.DEFAULT_CHARSET);
	}
	
	/**
	 * <p>将ByteBuffer转为字符串</p>
	 * 
	 * @param buffer ByteBuffer
	 * @param charset 编码
	 * 
	 * @return 字符串
	 */
	public static final String ofByteBuffer(ByteBuffer buffer, String charset) {
		if(buffer == null) {
			return null;
		}
		if(charset == null) {
			charset = SystemConfig.DEFAULT_CHARSET;
		}
		if(buffer.position() != 0) {
			buffer.flip();
		}
		String content = null;
		final CharsetDecoder decoder = Charset.forName(charset).newDecoder();
		decoder.onMalformedInput(CodingErrorAction.IGNORE);
		try {
			content = decoder.decode(buffer).toString();
			// 丢弃已经读取数据
			buffer.compact();
		} catch (CharacterCodingException e) {
			LOGGER.error("将ByteBuffer转为字符串异常", e);
		}
		return content;
	}
	
	/**
	 * <p>将输入流转为字符串</p>
	 * 
	 * @param input 输入流（不要关闭）
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
			LOGGER.error("将输入流转为字符串异常", e);
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
		if(startsWith(value, key)) {
			// 去掉键值
			value = value.substring(key.length()).trim();
			final String equalsSign = SymbolConfig.Symbol.EQUALS.toString();
			if(startsWith(value, equalsSign)) {
				// 去掉等号
				return value.substring(equalsSign.length()).trim();
			}
		}
		return null;
	}
	
	/**
	 * <p>获取文本原始编码</p>
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
		final var gbkEncoder = Charset.forName(SystemConfig.CHARSET_GBK).newEncoder();
		// GBK能够编码：原始默认UTF-8
		if(gbkEncoder.canEncode(content)) {
			return SystemConfig.CHARSET_UTF8;
		}
		final String gbkContent = StringUtils.charsetTo(content, SystemConfig.CHARSET_GBK);
		// 转为GBK字符编码能够编码：原始默认GBK
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
		if(object instanceof byte[] bytes) {
			if(encoding != null) {
				try {
					return new String(bytes, encoding);
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("字符编码异常：{}", encoding, e);
				}
			}
			return new String(bytes);
		} else {
			return object.toString();
		}
	}
	
	/**
	 * <p>将对象转为字符串（自动获取编码）</p>
	 * 
	 * @param object 对象
	 * @param encoding 编码
	 * 
	 * @return 字符串
	 * 
	 * @see #getCharset(String)
	 * @see #getString(Object, String)
	 */
	public static final String getCharsetString(Object object, String encoding) {
		if(encoding != null) {
			return getString(object, encoding);
		} else {
			// 没有编码自动获取
			final String utf8String = getString(object, encoding);
			final String charset = getCharset(utf8String);
			if(SystemConfig.CHARSET_GBK.equals(charset)) {
				return getString(object, SystemConfig.CHARSET_GBK);
			}
			return utf8String;
		}
	}
	
	/**
	 * <p>去掉字符串所有空白字符</p>
	 * 
	 * @param content 原始内容
	 * 
	 * @return 目标内容
	 */
	public static final String trimAllBlank(String content) {
		if(content == null) {
			return content;
		}
		return content.replaceAll(BLANK_REGEX, "");
	}
	
	/**
	 * <p>按行读取文本</p>
	 * 
	 * @param content 文本
	 * 
	 * @return 每行列表
	 */
	public static final List<String> readLines(String content) {
		if(content == null) {
			return List.of();
		}
		return Stream.of(content.split(SymbolConfig.Symbol.LINE_SEPARATOR.toString()))
			 .map(String::trim)
			 .filter(StringUtils::isNotEmpty)
			 .collect(Collectors.toList());
	}
	
}
