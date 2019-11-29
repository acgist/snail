package com.acgist.snail.system.bencode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.system.exception.PacketSizeException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>B编码解码器</p>
 * <dl>
 * 	<dt>类型</dt>
 * 	<dd>i：数字（Long）</dd>
 * 	<dd>l：列表：list</dd>
 * 	<dd>d：字典：map</dd>
 * 	<dd>e：结尾</dd>
 * </dl>
 * <p>所有类型除了Long，其他均为byte[]，需要自己进行类型转换。</p>
 * <p>解析前必须调用{@link #nextType()}、{@link #nextMap()}、{@link #nextList()}任一方法</p>
 * 
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class BEncodeDecoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BEncodeDecoder.class);
	
	/**
	 * B编码数据类型
	 */
	public enum Type {
		
		/** map */
		MAP,
		/** list */
		LIST,
		/** 未知 */
		NONE;
		
	}
	
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
	
	/**
	 * 数据类型
	 */
	private Type type;
	/**
	 * list
	 */
	private List<Object> list;
	/**
	 * map
	 */
	private Map<String, Object> map;
	/**
	 * 原始数据：不需要关闭
	 */
	private final ByteArrayInputStream inputStream;
	
	private BEncodeDecoder(byte[] bytes) {
		if(bytes == null) {
			throw new ArgumentException("B编码内容错误（bytes为空）");
		}
		if(bytes.length < 2) {
			throw new ArgumentException("B编码内容错误（长度）");
		}
		this.inputStream = new ByteArrayInputStream(bytes);
	}
	
	public static final BEncodeDecoder newInstance(String content) {
		if(content == null) {
			throw new ArgumentException("B编码内容错误（content为空）");
		}
		return new BEncodeDecoder(content.getBytes());
	}
	
	public static final BEncodeDecoder newInstance(byte[] bytes) {
		return new BEncodeDecoder(bytes);
	}
	
	/**
	 * <p>是否含有更多数据</p>
	 */
	public boolean more() {
		return this.inputStream != null && this.inputStream.available() > 0;
	}
	
	/**
	 * <p>数据是否为空</p>
	 * 
	 * @return true-为空；false-非空；
	 */
	public boolean isEmpty() {
		if(this.type == Type.LIST) {
			return this.list == null;
		} else if(this.type == Type.MAP) {
			return this.map == null;
		} else {
			return true;
		}
	}
	
	/**
	 * <p>是否包含数据</p>
	 * 
	 * @return true-非空；false-为空；
	 */
	public boolean isNotEmpty() {
		return !isEmpty();
	}
	
	/**
	 * <p>下一个数据类型</p>
	 * <p>获取下一个数据类型，同时解析下一个数据。</p>
	 */
	public Type nextType() throws PacketSizeException {
		if(!more()) {
			LOGGER.warn("B编码没有更多数据");
			return this.type = Type.NONE;
		}
		char type = (char) this.inputStream.read();
		switch (type) {
		case TYPE_D:
			this.map = d(this.inputStream);
			return this.type = Type.MAP;
		case TYPE_L:
			this.list = l(this.inputStream);
			return this.type = Type.LIST;
		default:
			LOGGER.warn("B编码错误（类型未适配）：{}", type);
			return this.type = Type.NONE;
		}
	}
	
	/**
	 * <p>获取下一个List</p>
	 * <p>如果下一个数据类型不是List返回null</p>
	 */
	public List<Object> nextList() throws PacketSizeException {
		final var type = nextType();
		if(type == Type.LIST) {
			return this.list;
		}
		return List.of();
	}
	
	/**
	 * <p>获取下一个Map</p>
	 * <p>如果下一个数据类型不是Map返回null</p>
	 */
	public Map<String, Object> nextMap() throws PacketSizeException {
		final var type = nextType();
		if(type == Type.MAP) {
			return this.map;
		}
		return Map.of();
	}
	
	/**
	 * <p>读取剩余所有数据</p>
	 */
	public byte[] oddBytes() {
		if(this.inputStream == null) {
			return null;
		}
		return this.inputStream.readAllBytes();
	}

	/**
	 * <p>读取剩余所有数据并转为字符串</p>
	 */
	public String oddString() {
		final var bytes = oddBytes();
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
	}

	/**
	 * <p>数值</p>
	 */
	private static final Long i(ByteArrayInputStream inputStream) {
		int index;
		char indexChar;
		final StringBuilder valueBuilder = new StringBuilder();
		while((index = inputStream.read()) != -1) {
			indexChar = (char) index;
			if(indexChar == TYPE_E) {
				final var number = valueBuilder.toString();
				if(!StringUtils.isNumeric(number)) {
					throw new ArgumentException("B编码格式错误（数字）：" + number);
				}
				return Long.valueOf(number);
			} else {
				valueBuilder.append(indexChar);
			}
		}
		return 0L;
	}
	
	/**
	 * <p>map</p>
	 */
	private static final Map<String, Object> d(ByteArrayInputStream inputStream) throws PacketSizeException {
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
					LOGGER.warn("B编码错误（长度）：{}", lengthBuilder);
				}
				break;
			default:
				LOGGER.debug("B编码错误（类型不支持）：{}", indexChar);
				break;
			}
		}
		return map;
	}
	
	/**
	 * <p>list</p>
	 */
	private static final List<Object> l(ByteArrayInputStream inputStream) throws PacketSizeException {
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
					LOGGER.warn("B编码错误（长度）：{}", lengthBuilder);
				}
				break;
			default:
				LOGGER.debug("B编码错误（类型不支持）：{}", indexChar);
				break;
			}
		}
		return list;
	}
	
	/**
	 * <p>读取符合长度的字节数组</p>
	 * 
	 * @param lengthBuilder 长度字符串：获取长度后清空
	 * @param inputStream 字节流
	 * 
	 * @return 字节数组
	 * 
	 * @throws PacketSizeException 超过最大网络包大小
	 */
	private static final byte[] read(StringBuilder lengthBuilder, ByteArrayInputStream inputStream) throws PacketSizeException {
		final var number = lengthBuilder.toString();
		if(!StringUtils.isNumeric(number)) {
			throw new ArgumentException("B编码格式错误（数字）：" + number);
		}
		final int length = Integer.parseInt(number);
		if(length >= SystemConfig.MAX_NET_BUFFER_LENGTH) {
			throw new PacketSizeException(length);
		}
		lengthBuilder.setLength(0);
		final byte[] bytes = new byte[length];
		try {
			final int readLength = inputStream.read(bytes);
			if(readLength != length) {
				LOGGER.warn("B编码错误（读取长度和实际长度不符）：{}-{}", length, readLength);
			}
		} catch (IOException e) {
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
	 * <p>使用LinkedHashMap防止乱序（乱序后计算的hash值将会改变）</p>
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
	 * <p>字节数组转字符串</p>
	 */
	public static final String getString(Object object) {
		if(object == null) {
			return null;
		}
		final byte[] bytes = (byte[]) object;
		return new String(bytes);
	}
	
}
