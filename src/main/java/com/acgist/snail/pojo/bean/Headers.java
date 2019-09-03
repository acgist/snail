package com.acgist.snail.pojo.bean;

import java.util.HashMap;
import java.util.Map;

import com.acgist.snail.utils.StringUtils;

/**
 * <p>头信息</p>
 * <p>如果第一行不包含{@link #HEADER_SPLIT}则读取为协议信息。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class Headers {

	private boolean hasProtocol; // 是否含有协议
	private final String[] lines; // 行信息
	private final String protocol; // 协议
	private final Map<String, String> headers; // 头信息
	
	private static final String HEADER_LINE = "\n"; // 头信息换行符
	private static final String HEADER_SPLIT = ":"; // 头信息分隔符

	private Headers(String content) {
		if(StringUtils.isEmpty(content)) {
			this.lines = null;
		} else {
			this.lines = content.split(HEADER_LINE);
		}
		this.protocol = readProtocol();
		this.headers = readHeaders();
	}
	
	/**
	 * 读取协议
	 */
	private String readProtocol() {
		if(this.lines == null || this.lines.length == 0) {
			this.hasProtocol = false;
			return null;
		} else {
			final String firstLine = this.lines[0];
			if(firstLine == null ||
				firstLine.indexOf(HEADER_SPLIT) != -1) {
				this.hasProtocol = false;
				return null;
			} else {
				this.hasProtocol = true;
				return firstLine.trim();
			}
		}
	}
	
	/**
	 * 读取头信息
	 */
	private Map<String, String> readHeaders() {
		int index;
		String key, line, value;
		final Map<String, String> headers = new HashMap<>();
		if(this.lines == null) {
			return headers;
		}
		final int begin = this.hasProtocol ? 1 : 0;
		for (int jndex = begin; jndex < this.lines.length; jndex++) {
			line = this.lines[jndex];
			if(line == null) {
				continue;
			}
			line = line.trim();
			if(line.isEmpty()) {
				continue;
			}
			index = line.indexOf(HEADER_SPLIT);
			if(index == -1) {
				key = line.trim();
				value = null;
			} else if(index < line.length()) {
				key = line.substring(0, index).trim();
				value = line.substring(index + 1).trim();
			} else {
				key = line.substring(0, index).trim();
				value = "";
			}
			headers.put(key, value);
		}
		return headers;
	
	}

	public static final Headers newInstance(String content) {
		return new Headers(content);
	}

	/**
	 * 获取头信息
	 * 
	 * @param key 头信息名称，忽略大小写。
	 * 
	 * @return 头信息值
	 */
	public String header(String key) {
		if(this.headers == null) {
			return null;
		}
		final var value = this.headers.entrySet().stream()
			.filter(entry -> {
				return StringUtils.equalsIgnoreCase(key, entry.getKey());
			})
			.map(entry -> entry.getValue())
			.findFirst();
		if(value.isEmpty()) {
			return null;
		}
		return value.get();
	}
	
	/**
	 * @return 协议
	 */
	public String protocol() {
		return this.protocol;
	}
	
	/**
	 * @return 所有头信息
	 */
	public Map<String, String> allHeaders() {
		return this.headers;
	}
	
}
