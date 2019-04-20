package com.acgist.snail.net.bcode;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;

/**
 * 种子磁力链接HASH生成器：<br>
 * 40位hash：sha1生成，磁力链接使用
 * 20位hash：sha1生成编码为20位，tracker使用
 */
public class BCodeEncoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BCodeEncoder.class);
	
	private ByteArrayBuilder builder;
	
	private BCodeEncoder() {
		builder = new ByteArrayBuilder();
	}
	
	public static final BCodeEncoder newInstance() {
		return new BCodeEncoder();
	}

	/**
	 * 获取map
	 */
	public BCodeEncoder build(Map<?, ?> map) {
		builder.write(BCodeDecoder.TYPE_D);
		map.forEach((key, value) -> {
			String keyValue = (String) key;
			builder.write(String.valueOf(keyValue.getBytes().length).getBytes());
			builder.write(BCodeDecoder.SEPARATOR);
			builder.write(keyValue.getBytes());
			if(value instanceof Number) {
				builder.write(BCodeDecoder.TYPE_I);
				builder.write(value.toString().getBytes());
				builder.write(BCodeDecoder.TYPE_E);
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
					builder.write(String.valueOf(bytes.length).getBytes());
					builder.write(BCodeDecoder.SEPARATOR);
					builder.write(bytes);
				}
			}
		});
		builder.write(BCodeDecoder.TYPE_E);
		return this;
	}

	/**
	 * 获取list
	 */
	public BCodeEncoder build(List<?> list) {
		builder.write(BCodeDecoder.TYPE_L);
		list.forEach(value -> {
			if(value instanceof Number) {
				builder.write(BCodeDecoder.TYPE_I);
				builder.write(value.toString().getBytes());
				builder.write(BCodeDecoder.TYPE_E);
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
					builder.write(String.valueOf(bytes.length).getBytes());
					builder.write(BCodeDecoder.SEPARATOR);
					builder.write(bytes);
				}
			}
		});
		builder.write(BCodeDecoder.TYPE_E);
		return this;
	}
	
	public byte[] bytes() {
		return builder.toByteArray();
	}

}
