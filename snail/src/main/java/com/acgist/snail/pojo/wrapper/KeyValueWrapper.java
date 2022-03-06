package com.acgist.snail.pojo.wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Key-Value包装器</p>
 * 
 * @author acgist
 */
public final class KeyValueWrapper {

	/**
	 * <p>默认separator</p>
	 * 
	 * @see #separator
	 */
	private static final char DEFAULT_SEPARATOR = SymbolConfig.Symbol.AND.toChar();
	/**
	 * <p>默认kvSeparator</p>
	 * 
	 * @see #kvSeparator
	 */
	private static final char DEFAULT_KV_SEPARATOR = SymbolConfig.Symbol.EQUALS.toChar();

	/**
	 * <p>连接符</p>
	 */
	private final char separator;
	/**
	 * <p>Key-Value连接符</p>
	 */
	private final char kvSeparator;
	/**
	 * <p>编码数据</p>
	 */
	private final String content;
	/**
	 * <p>解码数据</p>
	 */
	private final Map<String, String> data;

	/**
	 * @param separator 连接符
	 * @param kvSeparator Key-Value连接符
	 * @param content 编码数据
	 * @param data 解码数据
	 */
	private KeyValueWrapper(char separator, char kvSeparator, String content, Map<String, String> data) {
		this.separator = separator;
		this.kvSeparator = kvSeparator;
		this.content = content;
		if(data == null) {
			this.data = new HashMap<>();
		} else {
			this.data = data;
		}
	}

	/**
	 * <p>新建工具</p>
	 * 
	 * @return {@link KeyValueWrapper}
	 */
	public static final KeyValueWrapper newInstance() {
		return new KeyValueWrapper(DEFAULT_SEPARATOR, DEFAULT_KV_SEPARATOR, null, null);
	}
	
	/**
	 * <p>新建工具</p>
	 * 
	 * @param content 解码数据
	 * 
	 * @return {@link KeyValueWrapper}
	 */
	public static final KeyValueWrapper newInstance(String content) {
		return new KeyValueWrapper(DEFAULT_SEPARATOR, DEFAULT_KV_SEPARATOR, content, null);
	}
	
	/**
	 * <p>新建工具</p>
	 * 
	 * @param data 编码数据
	 * 
	 * @return {@link KeyValueWrapper}
	 */
	public static final KeyValueWrapper newInstance(Map<String, String> data) {
		return new KeyValueWrapper(DEFAULT_SEPARATOR, DEFAULT_KV_SEPARATOR, null, data);
	}
	
	/**
	 * <p>新建工具</p>
	 * 
	 * @param separator 连接符
	 * @param kvSeparator Key-Value连接符
	 * 
	 * @return {@link KeyValueWrapper}
	 */
	public static final KeyValueWrapper newInstance(char separator, char kvSeparator) {
		return new KeyValueWrapper(separator, kvSeparator, null, null);
	}
	
	/**
	 * <p>新建工具</p>
	 * 
	 * @param separator 连接符
	 * @param kvSeparator Key-Value连接符
	 * @param content 编码数据
	 * 
	 * @return {@link KeyValueWrapper}
	 */
	public static final KeyValueWrapper newInstance(char separator, char kvSeparator, String content) {
		return new KeyValueWrapper(separator, kvSeparator, content, null);
	}
	
	/**
	 * <p>新建工具</p>
	 * 
	 * @param separator 连接符
	 * @param kvSeparator Key-Value连接符
	 * @param data 解码数据
	 * 
	 * @return {@link KeyValueWrapper}
	 */
	public static final KeyValueWrapper newInstance(char separator, char kvSeparator, Map<String, String> data) {
		return new KeyValueWrapper(separator, kvSeparator, null, data);
	}
	
	/**
	 * <p>数据编码</p>
	 * 
	 * @return 编码数据
	 */
	public String encode() {
		final StringBuilder builder = new StringBuilder();
		this.data.forEach((key, value) -> builder.append(key).append(this.kvSeparator).append(value).append(this.separator));
		final int length = builder.length();
		if(length > 0) {
			builder.setLength(length - 1);
		}
		return builder.toString();
	}

	/**
	 * <p>数据解码</p>
	 * 
	 * @return {@link KeyValueWrapper}
	 */
	public KeyValueWrapper decode() {
		if(StringUtils.isEmpty(this.content)) {
			return this;
		}
		int index;
		String key;
		String value;
		final String[] keyValues = this.content.split(String.valueOf(this.separator));
		for (String keyValue : keyValues) {
			keyValue = keyValue.strip();
			if(keyValue.isEmpty()) {
				continue;
			}
			index = keyValue.indexOf(this.kvSeparator);
			if(index < 0) {
				key = keyValue.strip();
				value = null;
			} else {
				key = keyValue.substring(0, index).strip();
				value = keyValue.substring(index + 1).strip();
			}
			this.data.put(key, value);
		}
		return this;
	}
	
	/**
	 * <p>获取数据</p>
	 * 
	 * @param key Key
	 * 
	 * @return Value
	 */
	public String get(String key) {
		return this.data.get(key);
	}

	/**
	 * <p>获取数据</p>
	 * 
	 * @param key Key（忽略大小写）
	 * 
	 * @return Value
	 */
	public String getIgnoreCase(String key) {
		return this.data.entrySet().stream()
			.filter(entry -> StringUtils.equalsIgnoreCase(entry.getKey(), key))
			.map(Entry::getValue)
			// 需要判断是否为空：空值转换异常
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}
	
	@Override
	public String toString() {
		return this.data == null ? null : this.data.toString();
	}
	
}
