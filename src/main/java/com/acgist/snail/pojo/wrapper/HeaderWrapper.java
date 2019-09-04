package com.acgist.snail.pojo.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>头信息</p>
 * <p>如果第一行不包含{@link #HEADER_SPLIT}则读取为协议信息。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class HeaderWrapper {

	private final String protocol; // 协议
	private final boolean hasProtocol; // 是否含有协议
	protected final Map<String, List<String>> headers; // 头信息
	
	private static final String HEADER_LINE = "\n"; // 头信息换行符
	private static final String HEADER_SPLIT = ":"; // 头信息分隔符
	private static final String HEADER_SPACE = " "; // 头信息空格
	
	private static final String HEADER_LINE_BUILDER = "\r\n"; // 头信息换行符

	protected HeaderWrapper(String content) {
		String[] lines;
		if(StringUtils.isEmpty(content)) {
			lines = null;
		} else {
			lines = content.split(HEADER_LINE);
		}
		this.protocol = buildProtocol(lines);
		this.hasProtocol = StringUtils.isNotEmpty(this.protocol);
		this.headers = buildHeaders(lines);
	}
	
	protected HeaderWrapper(Map<String, List<String>> headers) {
		this.protocol = null;
		this.hasProtocol = false;
		this.headers = headers;
	}
	
	protected HeaderWrapper(String protocol, Map<String, List<String>> headers) {
		this.protocol = protocol;
		this.hasProtocol = StringUtils.isNotEmpty(this.protocol);
		this.headers = headers;
	}
	
	/**
	 * 读取协议
	 */
	private String buildProtocol(String[] lines) {
		if(lines == null || lines.length == 0) {
			return null;
		} else {
			final String firstLine = lines[0];
			if(
				firstLine == null ||
				firstLine.indexOf(HEADER_SPLIT) != -1
			) {
				return null;
			} else {
				return firstLine.trim();
			}
		}
	}
	
	/**
	 * 读取头信息
	 */
	private Map<String, List<String>> buildHeaders(String[] lines) {
		int index;
		String key, line, value;
		List<String> list;
		final Map<String, List<String>> headers = new HashMap<>();
		if(lines == null) {
			return headers;
		}
		final int begin = this.hasProtocol ? 1 : 0;
		for (int jndex = begin; jndex < lines.length; jndex++) {
			line = lines[jndex];
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
			list = headers.get(key);
			if(list == null) {
				list = new ArrayList<>();
				headers.put(key, list);
			}
			if(value != null) {
				list.add(value);
			}
		}
		return headers;
	
	}

	/**
	 * 读取头信息
	 */
	public static final HeaderWrapper newInstance(String content) {
		return new HeaderWrapper(content);
	}

	/**
	 * 写出头信息
	 * 
	 * @param protocol 协议
	 */
	public static final HeaderWrapper newBuilder(String protocol) {
		return new HeaderWrapper(protocol, new LinkedHashMap<String, List<String>>());
	}
	
	/**
	 * 写出头信息
	 * 
	 * @param protocol 协议
	 */
	public static final HeaderWrapper newBuilder(String protocol, Map<String, List<String>> headers) {
		return new HeaderWrapper(protocol, headers);
	}

	/**
	 * 获取第一个头信息
	 * 
	 * @param key 头信息名称，忽略大小写。
	 * 
	 * @return 头信息值
	 */
	public String header(String key) {
		final var list = headerList(key);
		if(CollectionUtils.isEmpty(list)) {
			return null;
		}
		final String value = list.get(0);
		return value == null ? null : value.trim();
	}
	
	/**
	 * 获取头信息
	 * 
	 * @param key 头信息名称，忽略大小写。
	 * 
	 * @return 头信息值
	 */
	public List<String> headerList(String key) {
		if(isEmpty()) {
			return null;
		}
		final var optional = this.headers.entrySet().stream()
			.filter(entry -> {
				return StringUtils.equalsIgnoreCase(key, entry.getKey());
			})
			.map(entry -> entry.getValue())
			.findFirst();
		if(optional.isEmpty()) {
			return null;
		}
		return optional.get();
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
	public Map<String, List<String>> allHeaders() {
		return this.headers;
	}

	/**
	 * 设置头信息
	 * 
	 * @param key 名称
	 * @param value 值
	 */
	public HeaderWrapper header(String key, String value) {
		if(this.headers == null) {
			throw new ArgumentException("headers没有初始化");
		}
		var list = this.headers.get(key);
		if(list == null) {
			list = new ArrayList<>();
			this.headers.put(key, list);
		}
		list.add(value);
		return this;
	}
	
	/**
	 * 写出头信息
	 */
	public String build() {
		final StringBuilder builder = new StringBuilder();
		if(this.hasProtocol) {
			builder.append(this.protocol).append(HEADER_LINE_BUILDER);
		}
		if(isNotEmpty()) {
			this.headers.forEach((key, list) -> {
				if(CollectionUtils.isEmpty(list)) {
					builder.append(key).append(HEADER_SPLIT).append(HEADER_SPACE).append(HEADER_LINE_BUILDER);
				} else {
					list.forEach(value -> {
						builder.append(key).append(HEADER_SPLIT).append(HEADER_SPACE).append(value).append(HEADER_LINE_BUILDER);
					});
				}
			});
		}
		builder.append(HEADER_LINE_BUILDER);
		return builder.toString();
	}
	
	/**
	 * header数据是否为空
	 */
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(this.headers);
	}
	
	/**
	 * header数据是否不为空
	 */
	public boolean isNotEmpty() {
		return !isEmpty();
	}
	
	@Override
	public String toString() {
		return this.build();
	}
	
}
