package com.acgist.snail.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * B编码解码器<br>
 * i：数字（Long）<br>
 * l：列表：list<br>
 * d：字典：map<br>
 * e：结尾<br>
 * 所有值除了Long，其他均为byte[]<br>
 * 读取前必须取出第一个字符
 */
public class BCodeUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(BCodeUtils.class);
	
	public static final char TYPE_E = 'e';
	public static final char TYPE_I = 'i';
	public static final char TYPE_L = 'l';
	public static final char TYPE_D = 'd';
	
	/**
	 * 判断是否是map类型
	 */
	public static final boolean isMap(ByteArrayInputStream input) {
		char type = (char) input.read();
		return type == TYPE_D;
	}
	
	/**
	 * 数值
	 */
	public static final Long i(ByteArrayInputStream input) {
		int index;
		char indexChar;
		StringBuilder valueBuilder = new StringBuilder();
		while((index = input.read()) != -1) {
			indexChar = (char) index;
			if(indexChar == TYPE_E) {
				return Long.valueOf(valueBuilder.toString());
			} else {
				valueBuilder.append(indexChar);
			}
		}
		return 0L;
	}
	
	/**
	 * map
	 */
	public static final Map<String, Object> d(ByteArrayInputStream input) {
		int index;
		char indexChar;
		String key = null;
		final Map<String, Object> map = new LinkedHashMap<>();
		final StringBuilder lengthBuilder = new StringBuilder();
		while ((index = input.read()) != -1) {
			indexChar = (char) index;
			switch (indexChar) {
				case TYPE_I:
					if(key != null) {
						map.put(key, i(input));
					} else {
						LOGGER.warn("key=null跳过");
					}
					key = null;
					break;
				case TYPE_E:
					return map;
				case TYPE_L:
					if(key != null) {
						map.put(key, l(input));
					} else {
						LOGGER.warn("key=null跳过");
					}
					key = null;
					break;
				case TYPE_D:
					if(key != null) {
						map.put(key, d(input));
					} else {
						LOGGER.warn("key=null跳过");
					}
					key = null;
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					lengthBuilder.append(indexChar);
					break;
				case ':':
					if(lengthBuilder.length() > 0) {
						final int length = Integer.parseInt(lengthBuilder.toString());
						lengthBuilder.setLength(0);
						final byte[] bytes = new byte[length];
						try {
							input.read(bytes);
						} catch (IOException e) {
							LOGGER.error("B编码读取异常", e);
						}
						final String value = new String(bytes);
						if (key == null) {
							key = value;
						} else {
							if(key != null) {
								map.put(key, bytes);
							}
							key = null;
						}
						break;
					}
			}
		}
		return map;
	}
	
	/**
	 * list
	 */
	public static final List<Object> l(ByteArrayInputStream input) {
		int index;
		char indexChar;
		final List<Object> list = new ArrayList<Object>();
		final StringBuilder lengthBuilder = new StringBuilder();
		while ((index = input.read()) != -1) {
			indexChar = (char) index;
			switch (indexChar) {
				case TYPE_I:
					list.add(i(input));
					break;
				case TYPE_E:
					return list;
				case TYPE_L:
					list.add(l(input));
					break;
				case TYPE_D:
					list.add(d(input));
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					lengthBuilder.append(indexChar);
					break;
				case ':':
					if(lengthBuilder.length() > 0) {
						final int length = Integer.parseInt(lengthBuilder.toString());
						lengthBuilder.setLength(0);
						final byte[] bytes = new byte[length];
						try {
							input.read(bytes);
						} catch (IOException e) {
							LOGGER.error("B编码读取异常", e);
						}
						list.add(bytes);
						break;
					}
			}
		}
		return list;
	}
	
	public static final String getString(Object object) {
		if(object == null) {
			return null;
		}
		byte[] bytes = (byte[]) object;
		return new String(bytes);
	}
	
	public static final Integer getInteger(Map<?, ?> map, String key) {
		Long value = getLong(map, key);
		if(value == null) {
			return null;
		}
		return value.intValue();
	}
	
	public static final Long getLong(Map<?, ?> map, String key) {
		return (Long) map.get(key);
	}
	
	public static final String getString(Map<?, ?> map, String key) {
		var bytes = getBytes(map, key);
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
	}
	
	public static final byte[] getBytes(Map<?, ?> map, String key) {
		return (byte[]) map.get(key);
	}
	
}
