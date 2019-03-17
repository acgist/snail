package com.acgist.snail.protocol.torrent;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

/**
 * 种子磁力链接HASH生成器：<br>
 * 40位hash：sha1生成，磁力链接使用
 * 20位hash：sha1生成编码为20位，tracker使用
 */
public class InfoHashBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(InfoHashBuilder.class);
	
	private ByteArrayBuilder builder;
	
	private InfoHashBuilder() {
		builder = new ByteArrayBuilder();
	}
	
	public static final InfoHashBuilder newInstance() {
		return new InfoHashBuilder();
	}

	/**
	 * 获取infoHash
	 */
	public InfoHashBuilder build(Map<?, ?> map) {
		builder.write('d');
		map.forEach((key, value) -> {
			String keyValue = (String) key;
			builder.write(String.valueOf(keyValue.getBytes().length).getBytes());
			builder.write(':');
			builder.write(keyValue.getBytes());
			if(value instanceof Long) {
				builder.write('i');
				builder.write(value.toString().getBytes());
				builder.write('e');
			} else if(value instanceof Map) {
				build((Map<?, ?>) value);
			} else if(value instanceof List) {
				build((List<?>) value);
			} else if(value instanceof byte[]) {
				byte[] bytes = (byte[]) value;
				builder.write(String.valueOf(bytes.length).getBytes());
				builder.write(':');
				builder.write(bytes);
			} else {
				LOGGER.warn("InfoHash不支持的类型，key：{}，value：{}", key, value);
			}
		});
		builder.write('e');
		return this;
	}

	/**
	 * 获取infoHash
	 */
	public InfoHashBuilder build(List<?> list) {
		builder.write('l');
		list.forEach(value -> {
			if(value instanceof Long) {
				builder.write('i');
				builder.write(value.toString().getBytes());
				builder.write('e');
			} else if(value instanceof Map) {
				build((Map<?, ?>) value);
			} else if(value instanceof List) {
				build((List<?>) value);
			} else if(value instanceof byte[]) {
				byte[] bytes = (byte[]) value;
				builder.write(String.valueOf(bytes.length).getBytes());
				builder.write(':');
				builder.write(bytes);
			} else {
				LOGGER.warn("InfoHash不支持的类型，value：{}", value);
			}
		});
		builder.write('e');
		return this;
	}

	/**
	 * 创建infoHash
	 */
	public InfoHash buildInfoHash() {
		return InfoHash.newInstance(builder.toByteArray());
	}

}
