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
	
	private List<Object> list;
	private Map<String, Object> map;
	private final ByteArrayInputStream inputStream;
	
	private BCodeDecoder(byte[] bytes) {
		this.inputStream = new ByteArrayInputStream(bytes);
	}
	
	public static final BCodeDecoder newInstance(String content) {
		if(content == null) {
			throw new ArgumentException("B编码解码内容错误");
		}
		return new BCodeDecoder(content.getBytes());
	}
	
	public static final BCodeDecoder newInstance(byte[] bytes) {
		if(bytes == null) {
			throw new ArgumentException("B编码解码内容错误");
		}
		return new BCodeDecoder(bytes);
	}
	
	/**
	 * 是否含有更多数据
	 */
	public boolean more() {
		return this.inputStream != null && inputStream.available() > 0;
	}
	
	/**
	 * 下一个数据类型
	 */
	public Type nextType() {
		if(!more()) {
			LOGGER.warn("B编码解码没有跟多数据");
			return Type.none;
		}
		char type = (char) inputStream.read();
		switch (type) {
		case TYPE_D:
			this.map = d(inputStream);
			return Type.map;
		case TYPE_L:
			this.list = l(inputStream);
			return Type.list;
		default:
			LOGGER.warn("不支持B编码类型：{}", type);
			return Type.none;
		}
	}
	
	/**
	 * 获取下一个List，如果不是List返回null
	 */
	public List<Object> nextList() {
		var type = nextType();
		if(type == Type.list) {
			return this.list;
		}
		return null;
	}
	
	/**
	 * 获取下一个Map，如果不是Map返回null
	 */
	public Map<String, Object> nextMap() {
		var type = nextType();
		if(type == Type.map) {
			return this.map;
		}
		return null;
	}
	
	/**
	 * 读取剩余所有数据
	 */
	public byte[] oddBytes() {
		return inputStream.readAllBytes();
	}

	/**
	 * 错误格式信息输出
	 */
	public String obbString() {
		return new String(oddBytes());
	}

	/**
	 * 数值
	 */
	private static final Long i(ByteArrayInputStream inputStream) {
		int index;
		char indexChar;
		final StringBuilder valueBuilder = new StringBuilder();
		while((index = inputStream.read()) != -1) {
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
	private static final Map<String, Object> d(ByteArrayInputStream inputStream) {
		int index;
		char indexChar;
		String key = null;
		final Map<String, Object> map = new LinkedHashMap<>();
		final StringBuilder lengthBuilder = new StringBuilder();
		while ((index = inputStream.read()) != -1) {
			indexChar = (char) index;
			switch (indexChar) {
				case TYPE_I:
					if(key != null) {
						map.put(key, i(inputStream));
					} else {
						LOGGER.warn("key=null跳过");
					}
					key = null;
					break;
				case TYPE_E:
					return map;
				case TYPE_L:
					if(key != null) {
						map.put(key, l(inputStream));
					} else {
						LOGGER.warn("key=null跳过");
					}
					key = null;
					break;
				case TYPE_D:
					if(key != null) {
						map.put(key, d(inputStream));
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
							inputStream.read(bytes);
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
	private static final List<Object> l(ByteArrayInputStream inputStream) {
		int index;
		char indexChar;
		final List<Object> list = new ArrayList<Object>();
		final StringBuilder lengthBuilder = new StringBuilder();
		while ((index = inputStream.read()) != -1) {
			indexChar = (char) index;
			switch (indexChar) {
				case TYPE_I:
					list.add(i(inputStream));
					break;
				case TYPE_E:
					return list;
				case TYPE_L:
					list.add(l(inputStream));
					break;
				case TYPE_D:
					list.add(d(inputStream));
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
							inputStream.read(bytes);
						} catch (IOException e) {
							LOGGER.error("B编码解码异常", e);
						}
						list.add(bytes);
						break;
					}
			}
		}
		return list;
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
		if(map == null) {
			return null;
		}
		return (Long) map.get(key);
	}
	
	public static final String getString(Object object) {
		if(object == null) {
			return null;
		}
		byte[] bytes = (byte[]) object;
		return new String(bytes);
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
		if(map == null) {
			return null;
		}
		return (byte[]) map.get(key);
	}
	
	public List<Object> getList(String key) {
		return getList(this.map, key);
	}
	
	// TODO：泛型优化
	@SuppressWarnings("unchecked")
	public static final List<Object> getList(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		return (List<Object>) map.get(key);
	}
	
	public Map<String, Object> getMap(String key) {
		return getMap(this.map, key);
	}
	
	// TODO：泛型优化
	@SuppressWarnings("unchecked")
	public static final Map<String, Object> getMap(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		return (Map<String, Object>) map.get(key);
	}
	
	public void close() {
		try {
			this.inputStream.close();
		} catch (IOException e) {
			LOGGER.error("B编码字符流关闭异常", e);
		}
	}
	
}
