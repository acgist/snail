package com.acgist.snail.protocol.tracker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * B编码：<br>
 * i：数字<br>
 * l：列表：list<br>
 * d：字典：map
 */
public class TrackerDecoder {
	
	private static final List<String> KEYS = new ArrayList<>();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerDecoder.class);
	
	static {
		KEYS.add("failure reason"); // 失败原因
		KEYS.add("warnging message"); // 警告信息
		KEYS.add("interval"); // 下一次连接等待时间
		KEYS.add("min interval"); // 下一次连接等待最小时间
		KEYS.add("tracker id"); // tracker id
		KEYS.add("complete"); // 当前有多少个peer已经完成了整个共享文件的下载
		KEYS.add("incomplete"); // 当前有多少个peer还没有完成共享文件的下载
		KEYS.add("peers"); // 各个peer的IP和端口号
	}
	
	private Map<String, String> data = new HashMap<>();
	
	public static final TrackerDecoder newInstance() {
		return new TrackerDecoder();
	}

	/**
	 * 解码
	 */
	public void decode(String message) {
		try(
			ByteArrayInputStream input = new ByteArrayInputStream(message.getBytes());
		) {
			decode(input);
		} catch (IOException e) {
			LOGGER.error("B解码异常，解码内容：{}", message, e);
		}
	}
	
	public Map<String, String> data() {
		return this.data;
	}
	
	/**
	 * 解码
	 */
	private void decode(ByteArrayInputStream input) throws IOException {
		int index;
		char indexChar;
		String key = null;
		StringBuilder lengthBuilder = new StringBuilder();
		while ((index = input.read()) != -1) {
			indexChar = (char) index;
			switch (indexChar) {
				case 'i':
					StringBuilder valueBuilder = new StringBuilder();
					while((index = input.read()) != -1) {
						indexChar = (char) index;
						if(indexChar == 'e') {
							break;
						} else {
							valueBuilder.append(indexChar);
						}
					}
					setValue(key, valueBuilder.toString());
					break;
				case 'l':
				case 'd':
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
				case ':':
					int length = Integer.parseInt(lengthBuilder.toString());
					lengthBuilder.setLength(0);
					byte[] bytes = new byte[length];
					input.read(bytes);
					String value = new String(bytes);
					if (KEYS.contains(value)) {
						key = value;
					} else {
						setValue(key, value);
					}
					break;
			}
		}
	}
	
	/**
	 * 设置值
	 */
	private void setValue(String key, String value) {
		if(KEYS.contains(key)) {
			data.put(key, value);
		}
	}
	
}
