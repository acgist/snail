package com.acgist.snail.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.ByteUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>B编码解码器</p>
 * 
 * <p>除了Long其他类型均为byte[]</p>
 * 
 * @author acgist
 */
public final class BEncodeDecoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BEncodeDecoder.class);
	
	/**
	 * <p>B编码数据类型</p>
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * <p>Map</p>
		 */
		MAP,
		/**
		 * <p>List</p>
		 */
		LIST,
		/**
		 * <p>未知</p>
		 */
		NONE;
		
	}
	
	/**
	 * <p>结尾：{@value}</p>
	 */
	public static final char TYPE_E = 'e';
	/**
	 * <p>数值：{@value}</p>
	 */
	public static final char TYPE_I = 'i';
	/**
	 * <p>List：{@value}</p>
	 */
	public static final char TYPE_L = 'l';
	/**
	 * <p>Map：{@value}</p>
	 */
	public static final char TYPE_D = 'd';
	/**
	 * <p>分隔符：{@value}</p>
	 */
	public static final char SEPARATOR = ':';
	/**
	 * <p>B编码最短数据长度</p>
	 */
	private static final int MIN_CONTENT_LENGTH = 2;
	
	/**
	 * <p>数据类型</p>
	 */
	private Type type;
	/**
	 * <p>List</p>
	 */
	private List<Object> list;
	/**
	 * <p>Map</p>
	 */
	private Map<String, Object> map;
	/**
	 * <p>原始数据</p>
	 */
	private final ByteArrayInputStream inputStream;
	
	/**
	 * @param bytes 数据
	 */
	private BEncodeDecoder(byte[] bytes) {
		Objects.requireNonNull(bytes, "B编码内容错误");
		if(bytes.length < MIN_CONTENT_LENGTH) {
			throw new IllegalArgumentException("B编码内容错误");
		}
		this.inputStream = new ByteArrayInputStream(bytes);
	}
	
	/**
	 * <p>新建B编码解码器</p>
	 * 
	 * @param bytes 数据
	 * 
	 * @return {@link BEncodeDecoder}
	 */
	public static final BEncodeDecoder newInstance(byte[] bytes) {
		return new BEncodeDecoder(bytes);
	}
	
	/**
	 * <p>新建B编码解码器</p>
	 * 
	 * @param content 数据
	 * 
	 * @return {@link BEncodeDecoder}
	 */
	public static final BEncodeDecoder newInstance(String content) {
		Objects.requireNonNull(content, "B编码内容错误");
		return new BEncodeDecoder(content.getBytes());
	}
	
	/**
	 * <p>新建B编码解码器</p>
	 * 
	 * @param buffer 数据
	 * 
	 * @return {@link BEncodeDecoder}
	 */
	public static final BEncodeDecoder newInstance(ByteBuffer buffer) {
		Objects.requireNonNull(buffer, "B编码内容错误");
		final byte[] bytes = ByteUtils.remainingToBytes(buffer);
		return new BEncodeDecoder(bytes);
	}
	
	/**
	 * <p>判断是否没有数据</p>
	 * 
	 * @return 是否没有数据
	 */
	public boolean isEmpty() {
		if(this.type == null) {
			return true;
		}
		return switch (this.type) {
			case MAP -> MapUtils.isEmpty(this.map);
			case LIST -> CollectionUtils.isEmpty(this.list);
			default -> true;
		};
	}
	
	/**
	 * <p>判断是否含有数据</p>
	 * 
	 * @return 是否含有数据
	 */
	public boolean isNotEmpty() {
		return !this.isEmpty();
	}
	
	/**
	 * <p>解析数据</p>
	 * 
	 * @return {@link BEncodeDecoder}
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see #nextType()
	 */
	public BEncodeDecoder next() throws PacketSizeException {
		this.nextType();
		return this;
	}
	
	/**
	 * <p>解析数据并获取数据类型</p>
	 * 
	 * @return 数据类型
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	public Type nextType() throws PacketSizeException {
		// 是否含有数据
		final boolean noneData = this.inputStream == null || this.inputStream.available() <= 0;
		if(noneData) {
			this.type = Type.NONE;
			return this.type;
		}
		final char charType = (char) this.inputStream.read();
		switch (charType) {
		case TYPE_D:
			this.map = readMap(this.inputStream);
			this.type = Type.MAP;
			break;
		case TYPE_L:
			this.list = readList(this.inputStream);
			this.type = Type.LIST;
			break;
		default:
			LOGGER.warn("B编码错误（未知类型）：{}", charType);
			this.type = Type.NONE;
			break;
		}
		return this.type;
	}
	
	/**
	 * <p>解析数据并获取List</p>
	 * 
	 * @return List
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see #nextType()
	 */
	public List<Object> nextList() throws PacketSizeException {
		final var nextType = this.nextType();
		if(nextType == Type.LIST) {
			return this.list;
		}
		return List.of();
	}
	
	/**
	 * <p>解析数据并获取Map</p>
	 * 
	 * @return Map
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see #nextType()
	 */
	public Map<String, Object> nextMap() throws PacketSizeException {
		final var nextType = this.nextType();
		if(nextType == Type.MAP) {
			return this.map;
		}
		return Map.of();
	}
	
	/**
	 * <p>读取剩余所有字节数组</p>
	 * 
	 * @return 剩余所有字节数组
	 */
	public byte[] oddBytes() {
		if(this.inputStream == null) {
			return new byte[0];
		}
		return this.inputStream.readAllBytes();
	}

	/**
	 * <p>读取数值</p>
	 * 
	 * @param inputStream 数据
	 * 
	 * @return 数值
	 * 
	 * @see #TYPE_I
	 */
	private static final Long readLong(ByteArrayInputStream inputStream) {
		int index;
		char indexChar;
		final StringBuilder valueBuilder = new StringBuilder();
		while((index = inputStream.read()) != -1) {
			indexChar = (char) index;
			if(indexChar == TYPE_E) {
				final var number = valueBuilder.toString();
				if(!StringUtils.isNumeric(number)) {
					throw new IllegalArgumentException("B编码错误（数值）：" + number);
				}
				return Long.valueOf(number);
			} else {
				valueBuilder.append(indexChar);
			}
		}
		return 0L;
	}
	
	/**
	 * <p>读取List</p>
	 * 
	 * @param inputStream 数据
	 * 
	 * @return List
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see #TYPE_L
	 */
	private static final List<Object> readList(ByteArrayInputStream inputStream) throws PacketSizeException {
		int index;
		char indexChar;
		final List<Object> list = new ArrayList<>();
		final StringBuilder lengthBuilder = new StringBuilder();
		while ((index = inputStream.read()) != -1) {
			indexChar = (char) index;
			switch (indexChar) {
				case TYPE_E -> {
					return list;
				}
				case TYPE_I -> list.add(readLong(inputStream));
				case TYPE_L -> list.add(readList(inputStream));
				case TYPE_D -> list.add(readMap(inputStream));
				case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> lengthBuilder.append(indexChar);
				case SEPARATOR -> {
					if(lengthBuilder.length() > 0) {
						list.add(readBytes(lengthBuilder, inputStream));
					} else {
						LOGGER.warn("B编码错误（长度）：{}", lengthBuilder);
					}
				}
				default -> LOGGER.warn("B编码错误（未知类型）：{}", indexChar);
			}
		}
		return list;
	}
	
	/**
	 * <p>读取Map</p>
	 * 
	 * @param inputStream 数据
	 * 
	 * @return Map
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see #TYPE_D
	 */
	private static final Map<String, Object> readMap(ByteArrayInputStream inputStream) throws PacketSizeException {
		int index;
		char indexChar;
		String key = null;
		final Map<String, Object> map = new LinkedHashMap<>();
		final StringBuilder lengthBuilder = new StringBuilder();
		while ((index = inputStream.read()) != -1) {
			indexChar = (char) index;
			switch (indexChar) {
				case TYPE_E -> {
					return map;
				}
				case TYPE_I -> {
					if(key != null) {
						map.put(key, readLong(inputStream));
						key = null;
					} else {
						LOGGER.warn("B编码key为空跳过（I）");
					}
				}
				case TYPE_L -> {
					if(key != null) {
						map.put(key, readList(inputStream));
						key = null;
					} else {
						LOGGER.warn("B编码key为空跳过（L）");
					}
				}
				case TYPE_D -> {
					if(key != null) {
						map.put(key, readMap(inputStream));
						key = null;
					} else {
						LOGGER.warn("B编码key为空跳过（D）");
					}
				}
				case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> lengthBuilder.append(indexChar);
				case SEPARATOR -> {
					if(lengthBuilder.length() > 0) {
						final byte[] bytes = readBytes(lengthBuilder, inputStream);
						if (key == null) {
							key = new String(bytes);
						} else {
							map.put(key, bytes);
							key = null;
						}
					} else {
						LOGGER.warn("B编码错误（长度）：{}", lengthBuilder);
					}
				}
				default -> LOGGER.warn("B编码错误（未知类型）：{}", indexChar);
			}
		}
		return map;
	}
	
	/**
	 * <p>读取符合长度的字节数组</p>
	 * 
	 * @param lengthBuilder 字节数组长度
	 * @param inputStream 数据
	 * 
	 * @return 字节数组
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	private static final byte[] readBytes(StringBuilder lengthBuilder, ByteArrayInputStream inputStream) throws PacketSizeException {
		final var number = lengthBuilder.toString();
		if(!StringUtils.isNumeric(number)) {
			throw new IllegalArgumentException("B编码错误（数值）：" + number);
		}
		final int length = Integer.parseInt(number);
		PacketSizeException.verify(length);
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
	
	/**
	 * <p>获取对象</p>
	 * 
	 * @param key 键
	 * 
	 * @return 对象
	 */
	public Object get(String key) {
		return get(this.map, key);
	}
	
	/**
	 * <p>获取对象</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 对象
	 */
	public static final Object get(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		return map.get(key);
	}
	
	/**
	 * <p>获取字节</p>
	 * 
	 * @param key 键
	 * 
	 * @return 字节
	 */
	public Byte getByte(String key) {
		return getByte(this.map, key);
	}
	
	/**
	 * <p>获取字节</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 字节
	 */
	public static final Byte getByte(Map<?, ?> map, String key) {
		final Long value = getLong(map, key);
		if(value == null) {
			return null;
		}
		return value.byteValue();
	}
	
	/**
	 * <p>获取数值</p>
	 * 
	 * @param key 键
	 * 
	 * @return 数值
	 */
	public Integer getInteger(String key) {
		return getInteger(this.map, key);
	}
	
	/**
	 * <p>获取数值</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 数值
	 */
	public static final Integer getInteger(Map<?, ?> map, String key) {
		final Long value = getLong(map, key);
		if(value == null) {
			return null;
		}
		return value.intValue();
	}
	
	/**
	 * <p>获取数值</p>
	 * 
	 * @param key 键
	 * 
	 * @return 数值
	 */
	public Long getLong(String key) {
		return getLong(this.map, key);
	}
	
	/**
	 * <p>获取数值</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 数值
	 */
	public static final Long getLong(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		return (Long) map.get(key);
	}
	
	/**
	 * <p>获取字符串</p>
	 * 
	 * @param key 键
	 * 
	 * @return 字符串
	 */
	public String getString(String key) {
		return getString(this.map, key);
	}
	
	/**
	 * <p>获取字符串</p>
	 * 
	 * @param key 键
	 * @param encoding 编码
	 * 
	 * @return 字符串
	 */
	public String getString(String key, String encoding) {
		return getString(this.map, key, encoding);
	}
	
	/**
	 * <p>获取字符串</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 字符串
	 */
	public static final String getString(Map<?, ?> map, String key) {
		return getString(map, key, null);
	}
	
	/**
	 * <p>获取字符串</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * @param encoding 编码
	 * 
	 * @return 字符串
	 */
	public static final String getString(Map<?, ?> map, String key, String encoding) {
		final var bytes = getBytes(map, key);
		if(bytes == null) {
			return null;
		}
		return StringUtils.getCharsetString(bytes, encoding);
	}
	
	/**
	 * <p>获取字节数组</p>
	 * 
	 * @param key 键
	 * 
	 * @return 字节数组
	 */
	public byte[] getBytes(String key) {
		return getBytes(this.map, key);
	}
	
	/**
	 * <p>获取字节数组</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 字节数组
	 */
	public static final byte[] getBytes(Map<?, ?> map, String key) {
		if(map == null) {
			return null;
		}
		return (byte[]) map.get(key);
	}
	
	/**
	 * <p>获取集合</p>
	 * 
	 * @param key 键
	 * 
	 * @return 集合
	 */
	public List<Object> getList(String key) {
		return getList(this.map, key);
	}
	
	/**
	 * <p>获取集合</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 集合
	 */
	public static final List<Object> getList(Map<?, ?> map, String key) {
		if(map == null) {
			return List.of();
		}
		final var result = (List<?>) map.get(key);
		if(result == null) {
			return List.of();
		}
		return result.stream()
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>获取Map</p>
	 * 
	 * @param key 键
	 * 
	 * @return Map
	 */
	public Map<String, Object> getMap(String key) {
		return getMap(this.map, key);
	}
	
	/**
	 * <p>获取Map</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return Map
	 */
	public static final Map<String, Object> getMap(Map<?, ?> map, String key) {
		if(map == null) {
			return Map.of();
		}
		final var result = (Map<?, ?>) map.get(key);
		if(result == null) {
			return Map.of();
		}
		// 使用LinkedHashMap防止乱序
		return result.entrySet().stream()
			.map(entry -> Map.entry(entry.getKey().toString(), entry.getValue()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
	}
	
	@Override
	public String toString() {
		return new String(this.oddBytes());
	}
	
}
