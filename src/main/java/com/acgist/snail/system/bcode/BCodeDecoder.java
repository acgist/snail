package com.acgist.snail.system.bcode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.ArgumentException;

/**
 * B编码解码器<br>
 * i：数字（Long）<br>
 * l：列表：list<br>
 * d：字典：map<br>
 * e：结尾<br>
 * 所有值除了Long，其他均为byte[]<br>
 * 读取前必须取出第一个字符
 */
public class BCodeDecoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BCodeDecoder.class);
	
	public static final char TYPE_E = 'e';
	public static final char TYPE_I = 'i';
	public static final char TYPE_L = 'l';
	public static final char TYPE_D = 'd';
	public static final char SEPARATOR = ':';
	
	public enum Type {
		map, // map
		list, // list
		none; // 未知
	}
	
	private Map<String, Object> map;
	
	private final ByteArrayInputStream input;
	
	public static final BCodeDecoder newInstance(byte[] bytes) {
		return new BCodeDecoder(bytes);
	}
	
	private BCodeDecoder(byte[] bytes) {
		this.input = new ByteArrayInputStream(bytes);
	}
	
	/**
	 * 是否含有更多数据
	 */
	public boolean more() {
		return input.available() > 0;
	}
	
	/**
	 * 下一个数据类型
	 */
	public Type nextType() {
		if(input == null || input.available() <= 0) {
			return Type.none;
		}
		char type = (char) input.read();
		switch (type) {
		case TYPE_D:
			return Type.map;
		case TYPE_L:
			return Type.list;
		default:
			LOGGER.warn("不支持B编码类型：{}", type);
			return Type.none;
		}
	}
	
	/**
	 * 获取下一个map，必须先验证类型nextType
	 */
	public Map<String, Object> nextMap() {
		this.map = d(input);
		return this.map;
	}
	
	/**
	 * 获取下一个map，如果类型错误抛出异常
	 */
	public Map<String, Object> mustMap() {
		var type = nextType();
		if(type == Type.map) {
			return nextMap();
		}
		throw new ArgumentException("BCode解析Map类型错误：" + type);
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
				case SEPARATOR:
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
				case SEPARATOR:
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
	
	public byte[] oddBytes() {
		return input.readAllBytes();
	}
	
	public static final String getString(Object object) {
		if(object == null) {
			return null;
		}
		byte[] bytes = (byte[]) object;
		return new String(bytes);
	}
	
	public Byte getByte(String key) {
		return getByte(this.map, key);
	}
	
	public static final Byte getByte(Map<?, ?> map, String key) {
		final Long value = getLong(map, key);
		if(value == null) {
			return null;
		}
		return value.byteValue();
	}
	
	public Integer getInteger(String key) {
		return getInteger(this.map, key);
	}
	
	public static final Integer getInteger(Map<?, ?> map, String key) {
		final Long value = getLong(map, key);
		if(value == null) {
			return null;
		}
		return value.intValue();
	}
	
	public Long getLong(String key) {
		return getLong(this.map, key);
	}
	
	public static final Long getLong(Map<?, ?> map, String key) {
		return (Long) map.get(key);
	}
	
	public String getString(String key) {
		return getString(this.map, key);
	}
	
	public static final String getString(Map<?, ?> map, String key) {
		final var bytes = getBytes(map, key);
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
	}
	
	public byte[] getBytes(String key) {
		return getBytes(this.map, key);
	}
	
	public static final byte[] getBytes(Map<?, ?> map, String key) {
		return (byte[]) map.get(key);
	}
	
	public List<Object> getList(String key) {
		return getList(this.map, key);
	}
	
	// TODO：泛型优化
	@SuppressWarnings("unchecked")
	public static final List<Object> getList(Map<?, ?> map, String key) {
		return (List<Object>) map.get(key);
	}
	
	public Map<String, Object> getMap(String key) {
		return getMap(this.map, key);
	}
	
	// TODO：泛型优化
	@SuppressWarnings("unchecked")
	public static final Map<String, Object> getMap(Map<?, ?> map, String key) {
		return (Map<String, Object>) map.get(key);
	}
	
}
