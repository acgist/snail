package com.acgist.snail.pojo.wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.acgist.snail.utils.StringUtils;

/**
 * <p>Key-Value拼接拆解</p>
 * 
 * @author acgist
 */
public final class KeyValueWrapper {

	/**
	 * <p>默认separator</p>
	 * 
	 * @see #separator
	 */
	private static final char DEFAULT_SEPARATOR = '&';
	/**
	 * <p>默认kvSeparator</p>
	 * 
	 * @see #kvSeparator
	 */
	private static final char DEFAULT_KV_SEPARATOR = '=';

	/**
	 * <p>连接符</p>
	 */
	private final char separator;
	/**
	 * <p>Key-Value连接符</p>
	 */
	private final char kvSeparator;
	/**
	 * <p>数据</p>
	 */
	private final Map<String, String> data;

	/**
	 * @param separator 连接符
	 * @param kvSeparator Key-Value连接符
	 * @param data 数据
	 */
	private KeyValueWrapper(char separator, char kvSeparator, Map<String, String> data) {
		this.separator = separator;
		this.kvSeparator = kvSeparator;
		this.data = data;
	}

	/**
	 * <p>创建工具</p>
	 * 
	 * @return KeyValueWrapper
	 */
	public static final KeyValueWrapper newInstance() {
		return new KeyValueWrapper(DEFAULT_SEPARATOR, DEFAULT_KV_SEPARATOR, new HashMap<String, String>());
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param data 数据
	 * 
	 * @return KeyValueWrapper
	 */
	public static final KeyValueWrapper newInstance(Map<String, String> data) {
		return new KeyValueWrapper(DEFAULT_SEPARATOR, DEFAULT_KV_SEPARATOR, data);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param separator 连接符
	 * @param kvSeparator Key-Value连接符
	 * 
	 * @return KeyValueWrapper
	 */
	public static final KeyValueWrapper newInstance(char separator, char kvSeparator) {
		return new KeyValueWrapper(separator, kvSeparator, new HashMap<String, String>());
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param separator 连接符
	 * @param kvSeparator Key-Value连接符
	 * @param data 数据
	 * 
	 * @return KeyValueWrapper
	 */
	public static final KeyValueWrapper newInstance(char separator, char kvSeparator, Map<String, String> data) {
		return new KeyValueWrapper(separator, kvSeparator, data);
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
	 * @param content 数据
	 * 
	 * @return KeyValueWrapper
	 */
	public KeyValueWrapper decode(String content) {
		if(content == null) {
			return this;
		}
		int index;
		String key;
		String value;
		final String[] keyValues = content.split(String.valueOf(this.separator));
		for (String keyValue : keyValues) {
			keyValue = keyValue.trim();
			if(keyValue.isEmpty()) {
				continue;
			}
			index = keyValue.indexOf(this.kvSeparator);
			if(index < 0) {
				this.data.put(keyValue, null);
			} else {
				key = keyValue.substring(0, index).trim();
				value = keyValue.substring(index + 1).trim();
				this.data.put(key, value);
			}
		}
		return this;
	}
	
	/**
	 * <p>通过Key获取数据</p>
	 * 
	 * @param key Key
	 * 
	 * @return Value
	 */
	public String get(String key) {
		if(this.data == null) {
			return null;
		}
		return this.data.get(key);
	}

	/**
	 * <p>通过Key获取数据（忽略大小写）</p>
	 * 
	 * @param key Key
	 * 
	 * @return Value
	 */
	public String getIgnoreCase(String key) {
		if(this.data == null) {
			return null;
		}
		return this.data.entrySet().stream()
			.filter(entry -> StringUtils.equalsIgnoreCase(entry.getKey(), key))
			.map(Entry::getValue)
			.findFirst().orElse(null);
	}
	
	/**
	 * <p>清空数据</p>
	 * 
	 * @return KeyValueWrapper
	 */
	public KeyValueWrapper clean() {
		if(this.data != null) {
			this.data.clear();
		}
		return this;
	}
	
	@Override
	public String toString() {
		return this.data == null ? null : this.data.toString();
	}
	
}
