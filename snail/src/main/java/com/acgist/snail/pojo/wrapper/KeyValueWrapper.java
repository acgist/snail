package com.acgist.snail.pojo.wrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Key-Value拼接拆解</p>
 * 
 * @author acgist
 * @version 1.5.0
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
	 * <p>Key大写</p>
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
	 * @param keyUpper Key大写
	 * @param separator 每项连接符
	 * @param kvSeparator Key-Value连接符
	 * 
	 * @return 工具
	 */
	public static final KeyValueWrapper newInstance(boolean keyUpper, char separator, char kvSeparator) {
		return new KeyValueWrapper(keyUpper, separator, kvSeparator);
	}
	
	/**
	 * <p>编码</p>
	 * 
	 * @return 编码结果
	 */
	public String encode(Map<String, String> map) {
		if(map == null) {
			return null;
		}
		final StringBuilder builder = new StringBuilder();
		map.forEach((key, value) -> builder.append(key).append(this.kvSeparator).append(value).append(this.separator));
		final int length = builder.length();
		if(length > 0) {
			builder.setLength(length - 1);
		}
		return builder.toString();
	}

	/**
	 * <p>解码</p>
	 * 
	 * @return 解码数据
	 */
	public Map<String, String> decode(String content) {
		if(content == null) {
			return null;
		}
		final String[] kvs = content.split(String.valueOf(this.separator));
		final Map<String, String> map = new HashMap<String, String>();
		int index;
		String key;
		String value;
		for (String kv : kvs) {
			kv = kv.trim();
			if(kv.isEmpty()) {
				continue;
			}
			index = kv.indexOf(this.kvSeparator);
			if(index < 0) {
				map.put(kv, null);
			} else {
				key = kv.substring(0, index).trim();
				value = kv.substring(index + 1).trim();
				if(this.keyUpper) {
					key = key.toUpperCase();
				}
				map.put(key, value);
			}
		}
		return map;
	}
	
}
