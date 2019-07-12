package com.acgist.snail.system.bencode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.bencode.BEnodeDecoder.Type;

/**
 * <p>B编码</p>
 * <p>put系列方法配合flush使用。</p>
 * <p>支持数据类型：Number、String、byte[]。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class BEnodeEncoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BEnodeEncoder.class);
	
	private List<Object> list;
	private Map<String, Object> map;
	private BEnodeDecoder.Type type;
	private ByteArrayOutputStream outputStream;
	
	private BEnodeEncoder() {
		this.outputStream = new ByteArrayOutputStream();
	}
	
	public static final BEnodeEncoder newInstance() {
		return new BEnodeEncoder();
	}

	/**
	 * 新建Map
	 */
	public BEnodeEncoder newMap() {
		this.type = Type.map;
		this.map = new LinkedHashMap<>();
		return this;
	}

	/**
	 * 新建List
	 */
	public BEnodeEncoder newList() {
		this.type = Type.list;
		this.list = new ArrayList<>();
		return this;
	}
	
	/**
	 * 向List中添加数据
	 */
	public BEnodeEncoder put(Object value) {
		if(this.type == Type.list) {
			this.list.add(value);
		}
		return this;
	}
	
	/**
	 * 向List中添加数据
	 */
	public BEnodeEncoder put(List<?> list) {
		if(this.type == Type.list) {
			this.list.addAll(list);
		}
		return this;
	}
	
	/**
	 * 向Map中添加数据
	 */
	public BEnodeEncoder put(String key, Object value) {
		if(this.type == Type.map) {
			this.map.put(key, value);
		}
		return this;
	}
	
	/**
	 * 向Map中添加数据
	 */
	public BEnodeEncoder put(Map<String, Object> map) {
		if(this.type == Type.map) {
			this.map.putAll(map);
		}
		return this;
	}

	/**
	 * 将List和Map中的数据刷入字符流，配合put系列方法使用。
	 */
	public BEnodeEncoder flush() {
		if(this.type == Type.map) {
			this.build(this.map);
		} else if(this.type == Type.list) {
			this.build(this.list);
		} else {
			LOGGER.warn("B编码刷新时不存在类型");
		}
		return this;
	}
	
	/**
	 * 添加Map
	 */
	public BEnodeEncoder build(Map<?, ?> map) {
		if(map == null) {
			return this;
		}
		this.outputStream.write(BEnodeDecoder.TYPE_D);
		map.forEach((key, value) -> {
			final String keyValue = (String) key;
			final byte[] keyValues = keyValue.getBytes();
			this.write(String.valueOf(keyValues.length).getBytes());
			this.outputStream.write(BEnodeDecoder.SEPARATOR);
			this.write(keyValues);
			if(value instanceof Number) {
				this.outputStream.write(BEnodeDecoder.TYPE_I);
				this.write(value.toString().getBytes());
				this.outputStream.write(BEnodeDecoder.TYPE_E);
			} else if(value instanceof Map) {
				build((Map<?, ?>) value);
			} else if(value instanceof List) {
				build((List<?>) value);
			} else {
				byte[] bytes = null;
				if(value instanceof byte[]) {
					bytes = (byte[]) value;
				} else if(value instanceof String) {
					bytes = ((String) value).getBytes();
				} else {
					LOGGER.warn("BCode不支持的类型，key：{}，value：{}", key, value);
				}
				if(bytes != null) {
					this.write(String.valueOf(bytes.length).getBytes());
					this.outputStream.write(BEnodeDecoder.SEPARATOR);
					this.write(bytes);
				}
			}
		});
		this.outputStream.write(BEnodeDecoder.TYPE_E);
		return this;
	}

	/**
	 * 添加List
	 */
	public BEnodeEncoder build(List<?> list) {
		if(list == null) {
			return this;
		}
		this.outputStream.write(BEnodeDecoder.TYPE_L);
		list.forEach(value -> {
			if(value instanceof Number) {
				this.outputStream.write(BEnodeDecoder.TYPE_I);
				this.write(value.toString().getBytes());
				this.outputStream.write(BEnodeDecoder.TYPE_E);
			} else if(value instanceof Map) {
				build((Map<?, ?>) value);
			} else if(value instanceof List) {
				build((List<?>) value);
			} else {
				byte[] bytes = null;
				if(value instanceof byte[]) {
					bytes = (byte[]) value;
				} else if(value instanceof String) {
					bytes = ((String) value).getBytes();
				} else {
					LOGGER.warn("BCode不支持的类型，value：{}", value);
				}
				if(bytes != null) {
					this.write(String.valueOf(bytes.length).getBytes());
					this.outputStream.write(BEnodeDecoder.SEPARATOR);
					this.write(bytes);
				}
			}
		});
		this.outputStream.write(BEnodeDecoder.TYPE_E);
		return this;
	}
	
	/**
	 * 添加字符数组
	 */
	public BEnodeEncoder build(byte[] bytes) {
		write(bytes);
		return this;
	}
	
	/**
	 * 写入字符数组
	 */
	private void write(byte[] bytes) {
		if(bytes == null) {
			return;
		}
		try {
			this.outputStream.write(bytes);
		} catch (IOException e) {
			LOGGER.error("B编码输出异常", e);
		}
	}
	
	/**
	 * 获取字符流，获取后将关闭流。
	 */
	public byte[] bytes() {
		try {
			return this.outputStream.toByteArray();
		} finally {
			try {
				this.outputStream.close();
			} catch (IOException e) {
				LOGGER.error("关闭字符流异常", e);
			}
		}
	}

	/**
	 * 获取字符串，将关闭字符流。
	 */
	@Override
	public String toString() {
		return new String(bytes());
	}
	
	/**
	 * Map转为B编码字符
	 */
	public static final byte[] encodeMap(Map<?, ?> map) {
		return newInstance().build(map).bytes();
	}
	
	/**
	 * List转为B编码字符
	 */
	public static final byte[] encodeList(List<?> list) {
		return newInstance().build(list).bytes();
	}

}
