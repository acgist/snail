package com.acgist.snail.system.bcode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 种子磁力链接HASH生成器：<br>
 * 40位hash：sha1生成，磁力链接使用
 * 20位hash：sha1生成编码为20位，tracker使用
 */
public class BCodeEncoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BCodeEncoder.class);
	
	private ByteArrayOutputStream outputStream;
//	private ByteArrayBuilder builder;
	
	private BCodeEncoder() {
		outputStream = new ByteArrayOutputStream();
	}
	
	public static final BCodeEncoder newInstance() {
		return new BCodeEncoder();
	}
	
	/**
	 * Map转为字符数组
	 */
	public static final byte[] mapToBytes(Map<?, ?> map) {
		return newInstance().build(map).bytes();
	}

	/**
	 * 获取map
	 */
	public BCodeEncoder build(Map<?, ?> map) {
		outputStream.write(BCodeDecoder.TYPE_D);
		map.forEach((key, value) -> {
			String keyValue = (String) key;
			this.write(String.valueOf(keyValue.getBytes().length).getBytes());
			outputStream.write(BCodeDecoder.SEPARATOR);
			this.write(keyValue.getBytes());
			if(value instanceof Number) {
				outputStream.write(BCodeDecoder.TYPE_I);
				this.write(value.toString().getBytes());
				outputStream.write(BCodeDecoder.TYPE_E);
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
					outputStream.write(BCodeDecoder.SEPARATOR);
					this.write(bytes);
				}
			}
		});
		outputStream.write(BCodeDecoder.TYPE_E);
		return this;
	}

	/**
	 * 获取list
	 */
	public BCodeEncoder build(List<?> list) {
		outputStream.write(BCodeDecoder.TYPE_L);
		list.forEach(value -> {
			if(value instanceof Number) {
				outputStream.write(BCodeDecoder.TYPE_I);
				this.write(value.toString().getBytes());
				outputStream.write(BCodeDecoder.TYPE_E);
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
					outputStream.write(BCodeDecoder.SEPARATOR);
					this.write(bytes);
				}
			}
		});
		outputStream.write(BCodeDecoder.TYPE_E);
		return this;
	}
	
	private void write(byte[] bytes) {
		try {
			outputStream.write(bytes);
		} catch (IOException e) {
			LOGGER.error("B编码输出异常", e);
		}
	}
	
	public byte[] bytes() {
		return outputStream.toByteArray();
	}
	
	public String toString() {
		return new String(bytes());
	}

}
