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
 * <p>如果第一行不包含{@link #HEADER_DEFAULT_KV}则读取为协议信息</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class HeaderWrapper {
	
	/**
	 * 头信息分隔符
	 */
	private static final String DEFAULT_HEADER_KV = ":";
	/**
	 * 头信息填充符
	 */
	private static final String DEFAULT_HEADER_PADDING = " ";
	/**
	 * 头信息换行符：读取
	 */
	private static final String HEADER_LINE_READER = "\n";
	/**
	 * 头信息换行符：写出
	 */
	private static final String HEADER_LINE_WRITER = "\r\n";

	/**
	 * 头信息分隔符
	 */
	private final String headerKv;
	/**
	 * 头信息填充符
	 */
	private final String headerPadding;
	/**
	 * 协议
	 */
	private final String protocol;
	/**
	 * 是否含有协议
	 */
	private final boolean hasProtocol;
	/**
	 * 头信息
	 */
	protected final Map<String, List<String>> headers;

	protected HeaderWrapper(String content) {
		this(DEFAULT_HEADER_KV, content);
	}
	
	protected HeaderWrapper(String headerKv, String content) {
		String[] lines;
		if(StringUtils.isEmpty(content)) {
			lines = null;
		} else {
			lines = content.split(HEADER_LINE_READER);
		}
		this.headerKv = headerKv;
		this.headerPadding = DEFAULT_HEADER_PADDING;
		this.protocol = buildProtocol(lines);
		this.hasProtocol = StringUtils.isNotEmpty(this.protocol);
		this.headers = buildHeaders(lines);
	}
	
	protected HeaderWrapper(Map<String, List<String>> headers) {
		this(DEFAULT_HEADER_KV, DEFAULT_HEADER_PADDING, headers);
	}
	
	protected HeaderWrapper(String headerKv, String headerPadding, Map<String, List<String>> headers) {
		this.headerKv = headerKv;
		this.headerPadding = headerPadding;
		this.protocol = null;
		this.hasProtocol = false;
		this.headers = headers;
	}
	
	protected HeaderWrapper(String protocol, Map<String, List<String>> headers) {
		this(DEFAULT_HEADER_KV, DEFAULT_HEADER_PADDING, protocol, headers);
	}
	
	protected HeaderWrapper(String headerKv, String headerPadding, String protocol, Map<String, List<String>> headers) {
		this.headerKv = headerKv;
		this.headerPadding = headerPadding;
		this.protocol = protocol;
		this.hasProtocol = StringUtils.isNotEmpty(this.protocol);
		this.headers = headers;
	}
	
	public static HeaderWrapper newInstance(String content) {
		return new HeaderWrapper(content);
	}

	public static HeaderWrapper newInstance(String headerKv, String content) {
		return new HeaderWrapper(headerKv, content);
	}

	public static HeaderWrapper newBuilder(String protocol) {
		return new HeaderWrapper(protocol, new LinkedHashMap<String, List<String>>());
	}
	
	public static HeaderWrapper newBuilder(String protocol, Map<String, List<String>> headers) {
		return new HeaderWrapper(protocol, headers);
	}
	
	public static HeaderWrapper newBuilder(String headerKv, String headerPadding, String protocol) {
		return new HeaderWrapper(headerKv, headerPadding, protocol, new LinkedHashMap<String, List<String>>());
	}
	
	public static HeaderWrapper newBuilder(String headerKv, String headerPadding, String protocol, Map<String, List<String>> headers) {
		return new HeaderWrapper(headerKv, headerPadding, protocol, headers);
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
				firstLine.indexOf(this.headerKv) != -1
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
			index = line.indexOf(this.headerKv);
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
	 * 读取头信息（多个头信息时读取第一个）
	 * 
	 * @param key 头信息名称（忽略大小写）
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
	 * 读取头信息
	 * 
	 * @param key 头信息名称（忽略大小写）
	 * 
	 * @return 头信息值
	 */
	public List<String> headerList(String key) {
		if(isEmpty()) {
			return List.of();
		}
		final var optional = this.headers.entrySet().stream()
			.filter(entry -> {
				return StringUtils.equalsIgnoreCase(key, entry.getKey());
			})
			.map(entry -> entry.getValue())
			.findFirst();
		if(optional.isEmpty()) {
			return List.of();
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
			throw new ArgumentException("请求头（headers）未初始化");
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
			builder.append(this.protocol).append(HEADER_LINE_WRITER);
		}
		if(isNotEmpty()) {
			this.headers.forEach((key, list) -> {
				if(CollectionUtils.isEmpty(list)) {
					builder.append(key).append(this.headerKv).append(this.headerPadding).append(HEADER_LINE_WRITER);
				} else {
					list.forEach(value -> {
						builder.append(key).append(this.headerKv).append(this.headerPadding).append(value).append(HEADER_LINE_WRITER);
					});
				}
			});
		}
		builder.append(HEADER_LINE_WRITER);
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
