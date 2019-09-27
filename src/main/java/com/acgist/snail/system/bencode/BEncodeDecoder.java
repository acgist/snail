package com.acgist.snail.system.bencode;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.ArgumentException;

/**
 * <p>B编码解码器</p>
 * <p>
 * i：数字（Long）<br>
 * l：列表：list<br>
 * d：字典：map<br>
 * e：结尾
 * </p>
 * <p>
 * 所有值除了Long，其他均为byte[]，需要自己再次解析。
 * </p>
 * <p>
 * 解析前必须调用{@link #nextType()}、{@link #nextMap()}、{@link #nextList()}任一方法。
 * </p>
 * 
 * 
 * @author acgist
 * @since 1.0.0
 */
public class BEncodeDecoder implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(BEncodeDecoder.class);
	
	/**
	 * 结尾
	 */
	public static final char TYPE_E = 'e';
	/**
	 * 数字
	 */
	public static final char TYPE_I = 'i';
	/**
	 * list
	 */
	public static final char TYPE_L = 'l';
	/**
	 * map
	 */
	public static final char TYPE_D = 'd';
	/**
	 * 分隔符
	 */
	public static final char SEPARATOR = ':';
	
	public enum Type {
		
		/** map */
		map,
		/** list */
		list,
		/** 未知 */
		none;
		
	}
	
	private List<Object> list;
	private Map<String, Object> map;
	private final ByteArrayInputStream inputStream;
	
	private BEncodeDecoder(byte[] bytes) {
		if(bytes == null) {
			throw new ArgumentException("B编码内容错误");
		}
		if(bytes.length < 2) {
			throw new ArgumentException("B编码内容错误");
		}
		this.inputStream = new ByteArrayInputStream(bytes);
	}
	
	public static final BEncodeDecoder newInstance(String content) {
		if(content == null) {
			throw new ArgumentException("B编码内容错误");
		}
		return new BEncodeDecoder(content.getBytes());
	}
	
	public static final BEncodeDecoder newInstance(byte[] bytes) {
		return new BEncodeDecoder(bytes);
	}
	
	/**
	 * 是否含有更多数据
	 */
	public boolean more() {
		return this.inputStream != null && this.inputStream.available() > 0;
	}
	
	/**
	 * 下一个数据类型
	 */
	public Type nextType() {
		if(!more()) {
			LOGGER.warn("B编码没有更多数据");
			return Type.none;
		}
		char type = (char) this.inputStream.read();
		switch (type) {
		case TYPE_D:
			this.map = d(this.inputStream);
			return Type.map;
		case TYPE_L:
			this.list = l(this.inputStream);
			return Type.list;
		default:
			LOGGER.warn("B编码不支持的类型：{}", type);
			return Type.none;
		}
	}
	
	/**
	 * 获取下一个List，如果不是List返回null。
	 */
	public List<Object> nextList() {
		var type = nextType();
		if(type == Type.list) {
			return this.list;
		}
		return List.of();
	}
	
	/**
	 * 获取下一个Map，如果不是Map返回null。
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
		return this.inputStream.readAllBytes();
	}

	/**
	 * 错误格式信息输出
	 */
	public String oddString() {
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
			case TYPE_E:
				return map;
			case TYPE_I:
				if(key != null) {
					map.put(key, i(inputStream));
					key = null;
				} else {
					LOGGER.warn("B编码key=null跳过");
				}
				break;
			case TYPE_L:
				if(key != null) {
					map.put(key, l(inputStream));
					key = null;
				} else {
					LOGGER.warn("B编码key=null跳过");
				}
				break;
			case TYPE_D:
				if(key != null) {
					map.put(key, d(inputStream));
					key = null;
				} else {
					LOGGER.warn("B编码key=null跳过");
				}
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
					final byte[] bytes = read(lengthBuilder, inputStream);
					if (key == null) {
						key = new String(bytes);
					} else {
						map.put(key, bytes);
						key = null;
					}
				} else {
					LOGGER.warn("B编码长度错误：{}", lengthBuilder);
				}
				break;
			default:
				LOGGER.debug("B编码不支持的类型：{}", indexChar);
				break;
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
			case TYPE_E:
				return list;
			case TYPE_I:
				list.add(i(inputStream));
				break;
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
					final byte[] bytes = read(lengthBuilder, inputStream);
					list.add(bytes);
				} else {
					LOGGER.warn("B编码长度错误：{}", lengthBuilder);
				}
				break;
			default:
				LOGGER.debug("B编码不支持的类型：{}", indexChar);
				break;
			}
		}
		return list;
	}

	/**
	 * 读取符合长度的字符数组
	 * 
	 * @param lengthBuilder 长度字符串，获取长度后清空。
	 * @param inputStream 字符流
	 * 
	 * @return 字符数组
	 */
	private static final byte[] read(StringBuilder lengthBuilder, ByteArrayInputStream inputStream) {
		final int length = Integer.parseInt(lengthBuilder.toString());
		if(length >= SystemConfig.MAX_NET_BUFFER_SIZE) {
//			throw new OversizePacketException(length);
			throw new ArgumentException("超过最大网络包");
		}
		lengthBuilder.setLength(0);
		final byte[] bytes = new byte[length];
		try {
			final int readLength = inputStream.read(bytes);
			if(readLength != length) {
				LOGGER.warn("B编码读取长度和实际长度不符：{}-{}", length, readLength);
			}
		} catch (Exception e) {
			LOGGER.error("B编码读取异常", e);
		}
		return bytes;
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
	
	public static final List<Object> getList(Map<?, ?> map, String key) {
		if(map == null) {
			return List.of();
		}
		final var tmp = (List<?>) map.get(key);
		if(tmp == null) {
			return List.of();
		}
		return tmp.stream()
			.map(value -> value)
			.collect(Collectors.toList());
	}
	
	public Map<String, Object> getMap(String key) {
		return getMap(this.map, key);
	}
	
	/**
	 * 注意不能乱序：乱序后计算的hash值将会改变。
	 */
	public static final Map<String, Object> getMap(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		final var tmp = (Map<?, ?>) map.get(key);
		if(tmp == null) {
			return null;
		}
		return tmp.entrySet().stream()
			.filter(entry -> entry.getKey() != null)
			.map(entry -> {
				return Map.entry(entry.getKey().toString(), entry.getValue());
			})
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
	}
	
	/**
	 * 关闭流，ByteArrayInputStream和ByteArrayOutputStream不需要关闭流。
	 */
	@Override
	public void close() {
		try {
			this.inputStream.close();
		} catch (Exception e) {
			LOGGER.error("B编码字符流关闭异常", e);
		}
	}
	
}
