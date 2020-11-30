package com.acgist.snail.pojo.wrapper;

import java.util.HashMap;
import java.util.Map;

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
	 * <p>Key转为大写</p>
	 */
	private final boolean keyUpper;
	/**
	 * <p>每项连接符</p>
	 */
	private final char separator;
	/**
	 * <p>Key-Value连接符</p>
	 */
	private final char kvSeparator;

	/**
	 * @param keyUpper Key转为大写
	 * @param separator 每项连接符
	 * @param kvSeparator Key-Value连接符
	 */
	private KeyValueWrapper(boolean keyUpper, char separator, char kvSeparator) {
		this.keyUpper = keyUpper;
		this.separator = separator;
		this.kvSeparator = kvSeparator;
	}

	/**
	 * <p>创建工具</p>
	 * 
	 * @return 工具
	 */
	public static final KeyValueWrapper newInstance() {
		return new KeyValueWrapper(false, DEFAULT_SEPARATOR, DEFAULT_KV_SEPARATOR);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param separator 每项连接符
	 * @param kvSeparator Key-Value连接符
	 * 
	 * @return 工具
	 */
	public static final KeyValueWrapper newInstance(char separator, char kvSeparator) {
		return new KeyValueWrapper(false, separator, kvSeparator);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param keyUpper Key转为大写
	 * @param separator 每项连接符
	 * @param kvSeparator Key-Value连接符
	 * 
	 * @return 工具
	 */
	public static final KeyValueWrapper newInstance(boolean keyUpper, char separator, char kvSeparator) {
		return new KeyValueWrapper(keyUpper, separator, kvSeparator);
	}
	
	/**
	 * <p>数据编码</p>
	 * 
	 * @param map 数据
	 * 
	 * @return 编码数据
	 */
	public String encode(Map<String, String> map) {
		if(map == null) {
			return null;
		}
		final StringBuilder builder = new StringBuilder();
		map.forEach((key, value) -> builder.append(this.keyUpper(key)).append(this.kvSeparator).append(value).append(this.separator));
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
	 * @return 解码数据
	 */
	public Map<String, String> decode(String content) {
		if(content == null) {
			return null;
		}
		final String[] keyValues = content.split(String.valueOf(this.separator));
		final Map<String, String> map = new HashMap<>();
		int index;
		String key;
		String value;
		for (String keyValue : keyValues) {
			keyValue = keyValue.trim();
			if(keyValue.isEmpty()) {
				continue;
			}
			index = keyValue.indexOf(this.kvSeparator);
			if(index < 0) {
				map.put(keyValue, null);
			} else {
				key = keyValue.substring(0, index).trim();
				value = keyValue.substring(index + 1).trim();
				map.put(this.keyUpper(key), value);
			}
		}
		return map;
	}
	
	/**
	 * <p>Key转为大写</p>
	 * 
	 * @param key Key
	 * 
	 * @return 转换后的Key
	 */
	private String keyUpper(String key) {
		if(key == null) {
			return key;
		}
		if(this.keyUpper) {
			return key.toUpperCase();
		} else {
			return key;
		}
	}
	
}
