package com.acgist.snail.pojo.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>头部信息包装器</p>
 * <p>如果第一行不包含{@linkplain #headerSeparator 头部信息分隔符}则为协议信息</p>
 * 
 * @author acgist
 */
public class HeaderWrapper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HeaderWrapper.class);
	
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
	private static final String HEADER_LINE_READER = SystemConfig.LINE_SEPARATOR;
	/**
	 * <p>头部信息换行符（写出）：{@value}</p>
	 */
	private static final String HEADER_LINE_WRITER = SystemConfig.LINE_COMPAT_SEPARATOR;

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

	/**
	 * @param content 头部信息
	 */
	protected HeaderWrapper(String content) {
		this(DEFAULT_HEADER_SEPARATOR, DEFAULT_HEADER_PADDING, content);
	}
	
	/**
	 * @param headerSeparator 分隔符
	 * @param headerPadding 填充符
	 * @param content 头部信息
	 */
	protected HeaderWrapper(String headerSeparator, String headerPadding, String content) {
		String[] lines;
		if(StringUtils.isEmpty(content)) {
			lines = null;
		} else {
			lines = content.split(HEADER_LINE_READER);
		}
		this.headerSeparator = headerSeparator;
		this.headerPadding = headerPadding;
		this.protocol = this.buildProtocol(lines);
		this.haveProtocol = StringUtils.isNotEmpty(this.protocol);
		this.headers = this.buildHeaders(lines);
	}
	
	/**
	 * @param headers 头部信息
	 */
	protected HeaderWrapper(Map<String, List<String>> headers) {
		this(DEFAULT_HEADER_SEPARATOR, DEFAULT_HEADER_PADDING, null, headers);
	}
	
	/**
	 * @param protocol 协议
	 * @param headers 头部信息
	 */
	protected HeaderWrapper(String protocol, Map<String, List<String>> headers) {
		this(DEFAULT_HEADER_SEPARATOR, DEFAULT_HEADER_PADDING, protocol, headers);
	}
	
	/**
	 * @param headerSeparator 分隔符
	 * @param headerPadding 填充符
	 * @param headers 头部信息
	 */
	protected HeaderWrapper(String headerSeparator, String headerPadding, Map<String, List<String>> headers) {
		this(headerSeparator, headerPadding, null, headers);
	}
	
	/**
	 * @param headerSeparator 分隔符
	 * @param headerPadding 填充符
	 * @param protocol 协议
	 * @param headers 头部信息
	 */
	protected HeaderWrapper(String headerSeparator, String headerPadding, String protocol, Map<String, List<String>> headers) {
		this.headerSeparator = headerSeparator;
		this.headerPadding = headerPadding;
		this.protocol = protocol;
		this.haveProtocol = StringUtils.isNotEmpty(this.protocol);
		this.headers = headers;
	}
	
	/**
	 * @param content 头部信息
	 * 
	 * @return HeaderWrapper
	 */
	public static final HeaderWrapper newInstance(String content) {
		return new HeaderWrapper(content);
	}

	/**
	 * @param headerSeparator 分隔符
	 * @param headerPadding 填充符
	 * @param content 头部信息
	 * 
	 * @return HeaderWrapper
	 */
	public static final HeaderWrapper newInstance(String headerSeparator, String headerPadding, String content) {
		return new HeaderWrapper(headerSeparator, headerPadding, content);
	}

	/**
	 * @param protocol 协议
	 * 
	 * @return HeaderWrapper
	 */
	public static final HeaderWrapper newBuilder(String protocol) {
		return new HeaderWrapper(protocol, new LinkedHashMap<String, List<String>>());
	}
	
	/**
	 * @param protocol 协议
	 * @param headers 头部信息
	 * 
	 * @return HeaderWrapper
	 */
	public static final HeaderWrapper newBuilder(String protocol, Map<String, List<String>> headers) {
		return new HeaderWrapper(protocol, headers);
	}
	
	/**
	 * @param headerSeparator 分隔符
	 * @param headerPadding 填充符
	 * @param protocol 协议
	 * 
	 * @return HeaderWrapper
	 */
	public static final HeaderWrapper newBuilder(String headerSeparator, String headerPadding, String protocol) {
		return new HeaderWrapper(headerSeparator, headerPadding, protocol, new LinkedHashMap<String, List<String>>());
	}
	
	/**
	 * @param headerSeparator 分隔符
	 * @param headerPadding 填充符
	 * @param protocol 协议
	 * @param headers 头部信息
	 * 
	 * @return HeaderWrapper
	 */
	public static final HeaderWrapper newBuilder(String headerSeparator, String headerPadding, String protocol, Map<String, List<String>> headers) {
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
		String line;
		String key, value;
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
				LOGGER.warn("头部信息解析错误（没有分隔符）：{}", line);
				continue;
			} else if(index < line.length()) {
				key = line.substring(0, index).trim();
				value = line.substring(index + 1).trim();
			} else {
				key = line.substring(0, index).trim();
				value = "";
			}
			list = headers.computeIfAbsent(key, newKey -> new ArrayList<>());
			list.add(value);
		}
		return headers;
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
	 * <p>读取头部信息</p>
	 * <p>如果头部名称对应多个头部信息时读取第一个</p>
	 * 
	 * @param key 头部名称（忽略大小写）
	 * 
	 * @return 头部信息
	 */
	public String header(String key) {
		final var list = this.headerList(key);
		if(CollectionUtils.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}
	
	/**
	 * <p>读取头部信息</p>
	 * 
	 * @param key 头部名称（忽略大小写）
	 * 
	 * @return 头部信息集合
	 */
	public List<String> headerList(String key) {
		if(this.isEmpty()) {
			return List.of();
		}
		final var optional = this.headers.entrySet().stream()
			.filter(entry -> StringUtils.equalsIgnoreCase(key, entry.getKey()))
			.map(Entry::getValue)
			.findFirst();
		if(optional.isEmpty()) {
			return List.of();
		}
		return optional.get();
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
		Objects.requireNonNull(this.headers, "头部信息未初始化");
		final var list = this.headers.computeIfAbsent(key, newKey -> new ArrayList<>());
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
		if(this.isNotEmpty()) {
			this.headers.forEach((key, list) -> {
				if(CollectionUtils.isEmpty(list)) {
					builder.append(key).append(this.headerSeparator).append(this.headerPadding).append(HEADER_LINE_WRITER);
				} else {
					list.stream()
					.map(value -> value == null ? "" : value.trim())
					.forEach(value -> builder.append(key).append(this.headerSeparator).append(this.headerPadding).append(value).append(HEADER_LINE_WRITER));
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
		return !this.isEmpty();
	}
	
	@Override
	public String toString() {
		return this.build();
	}
	
}
