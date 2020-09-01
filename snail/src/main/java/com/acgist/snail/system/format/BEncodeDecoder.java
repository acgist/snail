package com.acgist.snail.system.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.PacketSizeException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>B编码解码器</p>
 * 
 * <table border="1">
 * 	<caption>类型</caption>
 * 	<tr>
 * 		<th>符号</th>
 * 		<th>类型</th>
 * 	</tr>
 * 	<tr>
 * 		<td align="center">{@code i}</td>
 * 		<td>数字：{@code Long}</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="center">{@code l}</td>
 * 		<td>列表：{@code List}</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="center">{@code d}</td>
 * 		<td>字典：{@code Map}</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="center">{@code e}</td>
 * 		<td>结尾</td>
 * 	</tr>
 * </table>
 * <p>所有类型除了{@code Long}，其他均为{@code byte[]}，需要自己进行类型转换。</p>
 * <p>解析前必须调用{@link #nextType()}、{@link #nextMap()}、{@link #nextList()}任一方法</p>
 * 
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class BEncodeDecoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BEncodeDecoder.class);
	
	/**
	 * <p>B编码数据类型</p>
	 */
	public enum Type {
		
		/** Map */
		MAP,
		/** List */
		LIST,
		/** 未知 */
		NONE;
		
	}
	
	/**
	 * <p>结尾：{@value}</p>
	 */
	public static final char TYPE_E = 'e';
	/**
	 * <p>数字：{@value}</p>
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
	 * <p>原始数据（不需要关闭）</p>
	 */
	private final ByteArrayInputStream inputStream;
	
	private BEncodeDecoder(byte[] bytes) {
		Objects.requireNonNull(bytes, "B编码内容错误（数据为空）");
		if(bytes.length < 2) {
			throw new IllegalArgumentException("B编码内容错误（数据长度）");
		}
		this.inputStream = new ByteArrayInputStream(bytes);
	}
	
	/**
	 * <p>创建B编码解码器</p>
	 * 
	 * @param content 数据
	 * 
	 * @return B编码解码器
	 */
	public static final BEncodeDecoder newInstance(String content) {
		Objects.requireNonNull(content, "B编码内容错误（数据为空）");
		return new BEncodeDecoder(content.getBytes());
	}
	
	/**
	 * <p>创建B编码解码器</p>
	 * 
	 * @param buffer 数据
	 * 
	 * @return B编码解码器
	 */
	public static final BEncodeDecoder newInstance(ByteBuffer buffer) {
		Objects.requireNonNull(buffer, "B编码内容错误（数据为空）");
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return new BEncodeDecoder(bytes);
	}
	
	/**
	 * <p>创建B编码解码器</p>
	 * 
	 * @param bytes 数据
	 * 
	 * @return B编码解码器
	 */
	public static final BEncodeDecoder newInstance(byte[] bytes) {
		return new BEncodeDecoder(bytes);
	}
	
	/**
	 * <p>判断是否含有更多数据</p>
	 * 
	 * @return 是否含有更多数据
	 */
	public boolean more() {
		return this.inputStream != null && this.inputStream.available() > 0;
	}
	
	/**
	 * <p>数据是否为空</p>
	 * 
	 * @return {@code true}-空；{@code false}-非空；
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
	 * <p>数据是否非空</p>
	 * 
	 * @return {@code true}-非空；{@code false}-空；
	 */
	public boolean isNotEmpty() {
		return !this.isEmpty();
	}
	
	/**
	 * <p>获取下一个数据类型</p>
	 * <p>获取下一个数据类型，同时解析下一个数据。</p>
	 * 
	 * @return 下一个数据类型
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	public Type nextType() throws PacketSizeException {
		if(!this.more()) {
			LOGGER.warn("B编码没有更多数据");
			return this.type = Type.NONE;
		}
		final char type = (char) this.inputStream.read();
		switch (type) {
		case TYPE_D:
			this.map = this.readMap(this.inputStream);
			return this.type = Type.MAP;
		case TYPE_L:
			this.list = this.readList(this.inputStream);
			return this.type = Type.LIST;
		default:
			LOGGER.warn("B编码错误（类型未适配）：{}", type);
			return this.type = Type.NONE;
		}
	}
	
	/**
	 * <p>获取下一个List</p>
	 * <p>如果下一个数据类型不是{@code List}返回空{@code List}</p>
	 * 
	 * @return 下一个List
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	public List<Object> nextList() throws PacketSizeException {
		final var type = this.nextType();
		if(type == Type.LIST) {
			return this.list;
		}
		return List.of();
	}
	
	/**
	 * <p>获取下一个Map</p>
	 * <p>如果下一个数据类型不是{@code Map}返回空{@code Map}</p>
	 * 
	 * @return 下一个Map
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	public Map<String, Object> nextMap() throws PacketSizeException {
		final var type = this.nextType();
		if(type == Type.MAP) {
			return this.map;
		}
		return Map.of();
	}
	
	/**
	 * <p>读取剩余所有数据</p>
	 * 
	 * @return 剩余所有数据
	 */
	public byte[] oddBytes() {
		if(this.inputStream == null) {
			return null;
		}
		return this.inputStream.readAllBytes();
	}

	/**
	 * <p>读取剩余所有数据并转为字符串</p>
	 * 
	 * @return 剩余所有数据字符串
	 */
	public String oddString() {
		final var bytes = this.oddBytes();
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
	}

	/**
	 * <p>读取数值：{@value #TYPE_I}</p>
	 * 
	 * @param inputStream 数据
	 * 
	 * @return 数值
	 */
	private Long readLong(ByteArrayInputStream inputStream) {
		int index;
		char indexChar;
		final StringBuilder valueBuilder = new StringBuilder();
		while((index = inputStream.read()) != -1) {
			indexChar = (char) index;
			if(indexChar == TYPE_E) {
				final var number = valueBuilder.toString();
				if(!StringUtils.isNumeric(number)) {
					throw new IllegalArgumentException("B编码格式错误（数字）：" + number);
				}
				return Long.valueOf(number);
			} else {
				valueBuilder.append(indexChar);
			}
		}
		return 0L;
	}
	
	/**
	 * <p>读取Map：{@value #TYPE_D}</p>
	 * 
	 * @param inputStream 数据
	 * 
	 * @return Map
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	private Map<String, Object> readMap(ByteArrayInputStream inputStream) throws PacketSizeException {
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
					map.put(key, this.readLong(inputStream));
					key = null;
				} else {
					LOGGER.warn("B编码key为空跳过");
				}
				break;
			case TYPE_L:
				if(key != null) {
					map.put(key, this.readList(inputStream));
					key = null;
				} else {
					LOGGER.warn("B编码key为空跳过");
				}
				break;
			case TYPE_D:
				if(key != null) {
					map.put(key, this.readMap(inputStream));
					key = null;
				} else {
					LOGGER.warn("B编码key为空跳过");
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
					final byte[] bytes = this.read(lengthBuilder, inputStream);
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
	 * <p>读取List：{@value #TYPE_L}</p>
	 * 
	 * @param inputStream 数据
	 * 
	 * @return List
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	private List<Object> readList(ByteArrayInputStream inputStream) throws PacketSizeException {
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
				list.add(this.readLong(inputStream));
				break;
			case TYPE_L:
				list.add(this.readList(inputStream));
				break;
			case TYPE_D:
				list.add(this.readMap(inputStream));
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
					final byte[] bytes = this.read(lengthBuilder, inputStream);
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
	 * @throws PacketSizeException 网络包大小异常
	 */
	private byte[] read(StringBuilder lengthBuilder, ByteArrayInputStream inputStream) throws PacketSizeException {
		final var number = lengthBuilder.toString();
		if(!StringUtils.isNumeric(number)) {
			throw new IllegalArgumentException("B编码格式错误（数字）：" + number);
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
		return StringUtils.getStringCharset(bytes, encoding);
	}
	
	/**
	 * <p>获取字符数组</p>
	 * 
	 * @param key 键
	 * 
	 * @return 字符数组
	 */
	public byte[] getBytes(String key) {
		return getBytes(this.map, key);
	}
	
	/**
	 * <p>获取字符数组</p>
	 * 
	 * @param map 数据
	 * @param key 键
	 * 
	 * @return 字符数组
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
			.map(value -> value)
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
	 * <p>使用LinkedHashMap防止乱序（乱序后计算的Hash值将会改变）</p>
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
		return result.entrySet().stream()
			.filter(entry -> entry.getKey() != null)
			.map(entry -> Map.entry(entry.getKey().toString(), entry.getValue()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
	}
	
}
