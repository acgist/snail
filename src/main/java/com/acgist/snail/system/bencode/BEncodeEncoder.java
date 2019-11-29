package com.acgist.snail.system.bencode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.bencode.BEncodeDecoder.Type;

/**
 * <p>B编码编码器</p>
 * <p>支持数据类型：Number、String、byte[]</p>
 * <pre>
 * encoder
 * 	.newList().put("1").put("2").flush()
 * 	.newMap().put("a", "b").put("c", "d").flush()
 * 	.toString();
 * 
 * encoder
 * 	.write(List.of("a", "b"))
 * 	.write(Map.of("1", "2"))
 * 	.toString();
 * </pre>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class BEncodeEncoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BEncodeEncoder.class);
	
	/**
	 * <p>list</p>
	 */
	private List<Object> list;
	/**
	 * <p>map</p>
	 */
	private Map<String, Object> map;
	/**
	 * <p>数据类型</p>
	 */
	private BEncodeDecoder.Type type;
	/**
	 * <p>输出数据：不需要关闭</p>
	 */
	private ByteArrayOutputStream outputStream;
	
	private BEncodeEncoder() {
		this.outputStream = new ByteArrayOutputStream();
	}
	
	public static final BEncodeEncoder newInstance() {
		return new BEncodeEncoder();
	}

	/**
	 * <p>新建List</p>
	 */
	public BEncodeEncoder newList() {
		this.type = Type.LIST;
		this.list = new ArrayList<>();
		return this;
	}
	
	/**
	 * <p>新建Map</p>
	 */
	public BEncodeEncoder newMap() {
		this.type = Type.MAP;
		this.map = new LinkedHashMap<>();
		return this;
	}
	
	/**
	 * <p>向List中添加数据</p>
	 */
	public BEncodeEncoder put(Object value) {
		if(this.type == Type.LIST) {
			this.list.add(value);
		}
		return this;
	}
	
	/**
	 * <p>向List中添加数据</p>
	 */
	public BEncodeEncoder put(List<?> list) {
		if(this.type == Type.LIST) {
			this.list.addAll(list);
		}
		return this;
	}
	
	/**
	 * <p>向Map中添加数据</p>
	 */
	public BEncodeEncoder put(String key, Object value) {
		if(this.type == Type.MAP) {
			this.map.put(key, value);
		}
		return this;
	}
	
	/**
	 * <p>向Map中添加数据</p>
	 */
	public BEncodeEncoder put(Map<String, Object> map) {
		if(this.type == Type.MAP) {
			this.map.putAll(map);
		}
		return this;
	}

	/**
	 * <p>将List和Map中的数据写入字符流（配合put系列方法使用）</p>
	 */
	public BEncodeEncoder flush() {
		if(this.type == Type.MAP) {
			this.write(this.map);
		} else if(this.type == Type.LIST) {
			this.write(this.list);
		} else {
			LOGGER.warn("B编码错误（类型未适配）：{}", this.type);
		}
		return this;
	}

	/**
	 * <p>写入List</p>
	 */
	public BEncodeEncoder write(List<?> list) {
		if(list == null) {
			return this;
		}
		this.write(BEncodeDecoder.TYPE_L);
		list.forEach(value -> {
			this.writeBEncodeValue(value);
		});
		this.write(BEncodeDecoder.TYPE_E);
		return this;
	}
	
	/**
	 * <p>写入Map</p>
	 */
	public BEncodeEncoder write(Map<?, ?> map) {
		if(map == null) {
			return this;
		}
		this.write(BEncodeDecoder.TYPE_D);
		map.forEach((key, value) -> {
			final String keyValue = key.toString();
			final byte[] keyValues = keyValue.getBytes();
			this.writeBEncodeBytes(keyValues);
			this.writeBEncodeValue(value);
		});
		this.write(BEncodeDecoder.TYPE_E);
		return this;
	}
	
	/**
	 * <p>写入字节数组</p>
	 */
	public BEncodeEncoder write(byte[] bytes) {
		try {
			if(bytes != null) {
				this.outputStream.write(bytes);
			}
		} catch (IOException e) {
			LOGGER.error("B编码输出异常", e);
		}
		return this;
	}
	
	/**
	 * <p>写入B编码对象</p>
	 */
	private void writeBEncodeValue(Object value) {
		if(value instanceof Number) {
			this.writeBEncodeNumber((Number) value);
		} else if(value instanceof byte[]) {
			this.writeBEncodeBytes((byte[]) value);
		} else if(value instanceof String) {
			this.writeBEncodeBytes(((String) value).getBytes());
		} else if(value instanceof List) {
			write((List<?>) value);
		} else if(value instanceof Map) {
			write((Map<?, ?>) value);
		} else {
			this.writeBEncodeBytes(new byte[] {});
			LOGGER.debug("B编码错误（类型未适配）：{}", value);
		}
	}
	
	/**
	 * <p>写入B编码数值</p>
	 */
	private void writeBEncodeNumber(Number number) {
		this.write(BEncodeDecoder.TYPE_I);
		this.write(number.toString().getBytes());
		this.write(BEncodeDecoder.TYPE_E);
	}
	
	/**
	 * <p>写入B编码字节数组</p>
	 */
	private void writeBEncodeBytes(byte[] bytes) {
		this.write(String.valueOf(bytes.length).getBytes());
		this.write(BEncodeDecoder.SEPARATOR);
		this.write(bytes);
	}
	
	/**
	 * <p>写入字符</p>
	 */
	private void write(char value) {
		this.outputStream.write(value);
	}
	
	/**
	 * <p>获取字符流</p>
	 */
	public byte[] bytes() {
		return this.outputStream.toByteArray();
	}

	/**
	 * <p>获取字符串</p>
	 */
	@Override
	public String toString() {
		return new String(bytes());
	}
	
	/**
	 * <p>List转为B编码字节数组</p>
	 */
	public static final byte[] encodeList(List<?> list) {
		return newInstance().write(list).bytes();
	}
	
	/**
	 * <p>List转为B编码字符串</p>
	 */
	public static final String encodeListString(List<?> list) {
		return new String(encodeList(list));
	}
	
	/**
	 * <p>Map转为B编码字节数组</p>
	 */
	public static final byte[] encodeMap(Map<?, ?> map) {
		return newInstance().write(map).bytes();
	}
	
	/**
	 * <p>Map转为B编码字符串</p>
	 */
	public static final String encodeMapString(Map<?, ?> map) {
		return new String(encodeMap(map));
	}
	
}
