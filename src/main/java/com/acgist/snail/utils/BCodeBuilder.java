package com.acgist.snail.utils;

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
public class BCodeBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(BCodeBuilder.class);
	
	private ByteArrayBuilder builder;
	
	private BCodeBuilder() {
		builder = new ByteArrayBuilder();
	}
	
	public static final BCodeBuilder newInstance() {
		return new BCodeBuilder();
	}

	/**
	 * 获取map
	 */
	public BCodeBuilder build(Map<?, ?> map) {
		builder.write('d');
		map.forEach((key, value) -> {
			String keyValue = (String) key;
			builder.write(String.valueOf(keyValue.getBytes().length).getBytes());
			builder.write(':');
			builder.write(keyValue.getBytes());
			if(value instanceof Number) {
				builder.write('i');
				builder.write(value.toString().getBytes());
				builder.write('e');
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
					builder.write(':');
					builder.write(bytes);
				}
			}
		});
		builder.write('e');
		return this;
	}

	/**
	 * 获取list
	 */
	public BCodeBuilder build(List<?> list) {
		builder.write('l');
		list.forEach(value -> {
			if(value instanceof Number) {
				builder.write('i');
				builder.write(value.toString().getBytes());
				builder.write('e');
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
					builder.write(':');
					builder.write(bytes);
				}
			}
		});
		builder.write('e');
		return this;
	}
	
	public byte[] bytes() {
		return builder.toByteArray();
	}

}
