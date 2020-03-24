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
 * <p>头部信息包装器</p>
 * <p>如果第一行不包含{@linkplain #headerSeparator 头部信息分隔符}则为协议信息</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class HeaderWrapper {
	
	/**
	 * <p>头部信息分隔符：{@value}</p>
	 */
	private static final String DEFAULT_HEADER_SEPARATOR = ":";
	/**
	 * <p>头部信息填充符：{@value}</p>
	 */
	private static final String DEFAULT_HEADER_PADDING = " ";
	/**
	 * <p>头部信息换行符（读取）：{@value}</p>
	 */
	private static final String HEADER_LINE_READER = "\n";
	/**
	 * <p>头部信息换行符（写出）：{@value}</p>
	 */
	private static final String HEADER_LINE_WRITER = "\r\n";

	/**
	 * <p>头部信息分隔符</p>
	 */
	private final String headerSeparator;
	/**
	 * <p>头部信息填充符</p>
	 */
	private final String headerPadding;
	/**
	 * <p>协议</p>
	 */
	private final String protocol;
	/**
	 * <p>是否含有协议</p>
	 */
	private final boolean haveProtocol;
	/**
	 * <p>头部信息</p>
	 */
	protected final Map<String, List<String>> headers;

	protected HeaderWrapper(String content) {
		this(DEFAULT_HEADER_SEPARATOR, DEFAULT_HEADER_PADDING, content);
	}
	
	protected HeaderWrapper(String headerSeparator, String headerPadding, String content) {
		String[] lines;
		if(StringUtils.isEmpty(content)) {
			lines = null;
		} else {
			lines = content.split(HEADER_LINE_READER);
		}
		this.headerSeparator = headerSeparator;
		this.headerPadding = headerPadding;
		this.protocol = buildProtocol(lines);
		this.haveProtocol = StringUtils.isNotEmpty(this.protocol);
		this.headers = buildHeaders(lines);
	}
	
	protected HeaderWrapper(Map<String, List<String>> headers) {
		this(DEFAULT_HEADER_SEPARATOR, DEFAULT_HEADER_PADDING, headers);
	}
	
	protected HeaderWrapper(String headerSeparator, String headerPadding, Map<String, List<String>> headers) {
		this.headerSeparator = headerSeparator;
		this.headerPadding = headerPadding;
		this.protocol = null;
		this.haveProtocol = false;
		this.headers = headers;
	}
	
	protected HeaderWrapper(String protocol, Map<String, List<String>> headers) {
		this(DEFAULT_HEADER_SEPARATOR, DEFAULT_HEADER_PADDING, protocol, headers);
	}
	
	protected HeaderWrapper(String headerSeparator, String headerPadding, String protocol, Map<String, List<String>> headers) {
		this.headerSeparator = headerSeparator;
		this.headerPadding = headerPadding;
		this.protocol = protocol;
		this.haveProtocol = StringUtils.isNotEmpty(this.protocol);
		this.headers = headers;
	}
	
	public static HeaderWrapper newInstance(String content) {
		return new HeaderWrapper(content);
	}

	public static HeaderWrapper newInstance(String headerSeparator, String headerPadding, String content) {
		return new HeaderWrapper(headerSeparator, headerPadding, content);
	}

	public static HeaderWrapper newBuilder(String protocol) {
		return new HeaderWrapper(protocol, new LinkedHashMap<String, List<String>>());
	}
	
	public static HeaderWrapper newBuilder(String protocol, Map<String, List<String>> headers) {
		return new HeaderWrapper(protocol, headers);
	}
	
	public static HeaderWrapper newBuilder(String headerSeparator, String headerPadding, String protocol) {
		return new HeaderWrapper(headerSeparator, headerPadding, protocol, new LinkedHashMap<String, List<String>>());
	}
	
	public static HeaderWrapper newBuilder(String headerSeparator, String headerPadding, String protocol, Map<String, List<String>> headers) {
		return new HeaderWrapper(headerSeparator, headerPadding, protocol, headers);
	}
	
	/**
	 * <p>读取协议</p>
	 * 
	 * @param lines 头部信息
	 * 
	 * @return 协议
	 */
	private String buildProtocol(String[] lines) {
		if(lines == null || lines.length == 0) {
			return null;
		} else {
			final String firstLine = lines[0];
			if(
				firstLine == null ||
				firstLine.indexOf(this.headerSeparator) != -1
			) {
				return null;
			} else {
				return firstLine.trim();
			}
		}
	}
	
	/**
	 * <p>读取头部信息</p>
	 * 
	 * @param lines 头部信息
	 * 
	 * @return 头部信息
	 */
	private Map<String, List<String>> buildHeaders(String[] lines) {
		int index;
		String key, line, value;
		List<String> list;
		final Map<String, List<String>> headers = new HashMap<>();
		if(lines == null) {
			return headers;
		}
		final int begin = this.haveProtocol ? 1 : 0; // 是否含有协议
		for (int jndex = begin; jndex < lines.length; jndex++) {
			line = lines[jndex];
			if(line == null) {
				continue;
			}
			line = line.trim();
			if(line.isEmpty()) {
				continue;
			}
			index = line.indexOf(this.headerSeparator);
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
	 * <p>读取头部信息</p>
	 * <p>如果头部名称对应多个头部信息时读取第一个</p>
	 * 
	 * @param key 头部名称（忽略大小写）
	 * 
	 * @return 头部信息
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
	 * <p>读取头部信息</p>
	 * 
	 * @param key 头部名称（忽略大小写）
	 * 
	 * @return 头部信息集合
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
	 * <p>获取协议</p>
	 * 
	 * @return 协议
	 */
	public String protocol() {
		return this.protocol;
	}
	
	/**
	 * <p>获取所有头部信息</p>
	 * 
	 * @return 所有头部信息
	 */
	public Map<String, List<String>> allHeaders() {
		return this.headers;
	}

	/**
	 * <p>设置头部信息<p>
	 * 
	 * @param key 名称
	 * @param value 信息
	 * 
	 * @return {@code this}
	 */
	public HeaderWrapper header(String key, String value) {
		if(this.headers == null) {
			throw new ArgumentException("头部信息未初始化");
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
	 * <p>写出头部信息文本</p>
	 * 
	 * @return 头部信息文本
	 */
	public String build() {
		final StringBuilder builder = new StringBuilder();
		if(this.haveProtocol) {
			builder.append(this.protocol).append(HEADER_LINE_WRITER);
		}
		if(isNotEmpty()) {
			this.headers.forEach((key, list) -> {
				if(CollectionUtils.isEmpty(list)) {
					builder.append(key).append(this.headerSeparator).append(this.headerPadding).append(HEADER_LINE_WRITER);
				} else {
					list.forEach(value -> {
						builder.append(key).append(this.headerSeparator).append(this.headerPadding).append(value).append(HEADER_LINE_WRITER);
					});
				}
			});
		}
		builder.append(HEADER_LINE_WRITER);
		return builder.toString();
	}
	
	/**
	 * <p>判断头部信息是否为空</p>
	 * 
	 * @return 是否为空
	 */
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(this.headers);
	}
	
	/**
	 * <p>判断头部信息是否含有数据</p>
	 * 
	 * @return 是否含有数据
	 */
	public boolean isNotEmpty() {
		return !isEmpty();
	}
	
	@Override
	public String toString() {
		return this.build();
	}
	
}
