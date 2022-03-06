package com.acgist.snail.pojo.wrapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>头部信息包装器</p>
 * 
 * @author acgist
 */
public class HeaderWrapper {
	
	/**
	 * <p>头部信息分隔符：{@value}</p>
	 */
	private static final String DEFAULT_HEADER_SEPARATOR = SymbolConfig.Symbol.COLON.toString();
	/**
	 * <p>头部信息填充符：{@value}</p>
	 */
	private static final String DEFAULT_HEADER_PADDING = SymbolConfig.Symbol.SPACE.toString();
	/**
	 * <p>头部信息换行符（读取）：{@value}</p>
	 */
	private static final String HEADER_LINE_READER = SymbolConfig.Symbol.LINE_SEPARATOR.toString();
	/**
	 * <p>头部信息换行符（写出）：{@value}</p>
	 */
	private static final String HEADER_LINE_WRITER = SymbolConfig.LINE_SEPARATOR_COMPAT;

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
	private final boolean hasProtocol;
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
		this.headerSeparator = headerSeparator;
		this.headerPadding = headerPadding;
		this.headers = new LinkedHashMap<>();
		if(StringUtils.isEmpty(content)) {
			this.protocol = null;
			this.hasProtocol = false;
		} else {
			final String[] lines = content.split(HEADER_LINE_READER);
			this.protocol = this.buildProtocol(lines);
			this.hasProtocol = StringUtils.isNotEmpty(this.protocol);
			this.buildHeaders(lines);
		}
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
	 * @param protocol 协议
	 * @param headers 头部信息
	 */
	protected HeaderWrapper(String headerSeparator, String headerPadding, String protocol, Map<String, List<String>> headers) {
		this.headerSeparator = headerSeparator;
		this.headerPadding = headerPadding;
		this.protocol = protocol;
		this.hasProtocol = StringUtils.isNotEmpty(this.protocol);
		this.headers = headers;
	}
	
	/**
	 * <p>新建头部信息解析器</p>
	 * 
	 * @param content 头部信息
	 * 
	 * @return HeaderWrapper
	 */
	public static final HeaderWrapper newInstance(String content) {
		return new HeaderWrapper(content);
	}

	/**
	 * <p>新建头部信息解析器</p>
	 * 
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
	 * <p>新建头部信息构建器</p>
	 * 
	 * @param protocol 协议
	 * 
	 * @return HeaderWrapper
	 */
	public static final HeaderWrapper newBuilder(String protocol) {
		return new HeaderWrapper(protocol, new LinkedHashMap<String, List<String>>());
	}
	
	/**
	 * <p>新建头部信息构建器</p>
	 * 
	 * @param protocol 协议
	 * @param headers 头部信息
	 * 
	 * @return HeaderWrapper
	 */
	public static final HeaderWrapper newBuilder(String protocol, Map<String, List<String>> headers) {
		return new HeaderWrapper(protocol, headers);
	}
	
	/**
	 * <p>新建头部信息构建器</p>
	 * 
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
	 * <p>新建头部信息构建器</p>
	 * 
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
	 * <p>解析协议</p>
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
				return firstLine.strip();
			}
		}
	}
	
	/**
	 * <p>解析头部信息</p>
	 * 
	 * @param lines 头部信息
	 */
	private void buildHeaders(String[] lines) {
		if(ArrayUtils.isEmpty(lines)) {
			return;
		}
		int index;
		String line;
		String key;
		String value;
		List<String> list;
		// 是否含有协议
		final int begin = this.hasProtocol ? 1 : 0;
		for (int jndex = begin; jndex < lines.length; jndex++) {
			line = lines[jndex];
			if(StringUtils.isEmpty(line)) {
				continue;
			}
			index = line.indexOf(this.headerSeparator);
			if(index == -1) {
				key = line.strip();
				value = "";
			} else if(index < line.length()) {
				key = line.substring(0, index).strip();
				value = line.substring(index + 1).strip();
			} else {
				key = line.substring(0, index).strip();
				value = "";
			}
			list = this.headers.computeIfAbsent(key, newKey -> new ArrayList<>());
			list.add(value);
		}
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
	 * <p>获取头部信息</p>
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
		final String value = list.get(0);
		return value == null ? value : value.strip();
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
		return this.headers.entrySet().stream()
			.filter(entry -> StringUtils.equalsIgnoreCase(key, entry.getKey()))
			.map(Entry::getValue)
			// 需要判断是否为空
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(List.of());
	}

	/**
	 * <p>设置头部信息<p>
	 * 
	 * @param key 名称
	 * @param value 信息
	 * 
	 * @return {@link HeaderWrapper}
	 */
	public HeaderWrapper header(String key, String value) {
		Objects.requireNonNull(this.headers, "头部信息未初始化");
		final var list = this.headers.computeIfAbsent(key, newKey -> new ArrayList<>());
		list.add(value);
		return this;
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
	 * <p>新建头部信息文本</p>
	 * 
	 * @return 头部信息文本
	 */
	public String build() {
		final StringBuilder builder = new StringBuilder();
		if(this.hasProtocol) {
			builder.append(this.protocol).append(HEADER_LINE_WRITER);
		}
		if(this.isNotEmpty()) {
			this.headers.forEach((key, list) -> {
				if(CollectionUtils.isEmpty(list)) {
					builder.append(key).append(this.headerSeparator).append(this.headerPadding).append(HEADER_LINE_WRITER);
				} else {
					list.stream()
					.map(value -> value == null ? "" : value.strip())
					.forEach(value -> builder.append(key).append(this.headerSeparator).append(this.headerPadding).append(value).append(HEADER_LINE_WRITER));
				}
			});
		}
		return builder.toString();
	}
	
	/**
	 * <p>判断头部信息是否为空</p>
	 * 
	 * @return 是否为空
	 */
	public boolean isEmpty() {
		return MapUtils.isEmpty(this.headers);
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
