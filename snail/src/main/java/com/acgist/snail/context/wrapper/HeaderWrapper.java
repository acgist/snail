package com.acgist.snail.context.wrapper;

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
 * 头部信息包装器
 * 
 * @author acgist
 */
public class HeaderWrapper {
    
    /**
     * 头部信息分隔符
     */
    private static final String DEFAULT_HEADER_SEPARATOR = SymbolConfig.Symbol.COLON.toString();
    /**
     * 头部信息填充符
     */
    private static final String DEFAULT_HEADER_PADDING = SymbolConfig.Symbol.SPACE.toString();
    /**
     * 头部信息换行符（读取）
     */
    private static final String HEADER_LINE_READER = SymbolConfig.Symbol.LINE_SEPARATOR.toString();
    /**
     * 头部信息换行符（写出）
     */
    private static final String HEADER_LINE_WRITER = SymbolConfig.LINE_SEPARATOR_COMPAT;

    /**
     * 分隔符
     */
    protected final String headerSeparator;
    /**
     * 填充符
     */
    protected final String headerPadding;
    /**
     * 协议
     */
    protected final String protocol;
    /**
     * 是否含有协议
     * 读取头部首行如果没有分隔符表示协议
     */
    protected final boolean hasProtocol;
    /**
     * 头部信息
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
     * @param headerPadding   填充符
     * @param content         头部信息
     */
    protected HeaderWrapper(String headerSeparator, String headerPadding, String content) {
        this.headerSeparator = headerSeparator;
        this.headerPadding   = headerPadding;
        this.headers         = new LinkedHashMap<>();
        if(StringUtils.isEmpty(content)) {
            this.protocol    = null;
            this.hasProtocol = false;
        } else {
            final String[] lines = content.split(HEADER_LINE_READER);
            this.protocol        = this.buildProtocol(lines);
            this.hasProtocol     = StringUtils.isNotEmpty(this.protocol);
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
     * @param headers  头部信息
     */
    protected HeaderWrapper(String protocol, Map<String, List<String>> headers) {
        this(DEFAULT_HEADER_SEPARATOR, DEFAULT_HEADER_PADDING, protocol, headers);
    }
    
    /**
     * @param headerSeparator 分隔符
     * @param headerPadding   填充符
     * @param protocol        协议
     * @param headers         头部信息
     */
    protected HeaderWrapper(String headerSeparator, String headerPadding, String protocol, Map<String, List<String>> headers) {
        this.headerSeparator = headerSeparator;
        this.headerPadding   = headerPadding;
        this.protocol        = protocol;
        this.hasProtocol     = StringUtils.isNotEmpty(this.protocol);
        this.headers         = headers;
    }
    
    /**
     * @param content 头部信息
     * 
     * @return {@link HeaderWrapper}
     */
    public static final HeaderWrapper newInstance(String content) {
        return new HeaderWrapper(content);
    }

    /**
     * @param headerSeparator 分隔符
     * @param headerPadding   填充符
     * @param content         头部信息
     * 
     * @return {@link HeaderWrapper}
     */
    public static final HeaderWrapper newInstance(String headerSeparator, String headerPadding, String content) {
        return new HeaderWrapper(headerSeparator, headerPadding, content);
    }

    /**
     * @param protocol 协议
     * 
     * @return {@link HeaderWrapper}
     */
    public static final HeaderWrapper newBuilder(String protocol) {
        return new HeaderWrapper(protocol, new LinkedHashMap<String, List<String>>());
    }
    
    /**
     * @param protocol 协议
     * @param headers  头部信息
     * 
     * @return {@link HeaderWrapper}
     */
    public static final HeaderWrapper newBuilder(String protocol, Map<String, List<String>> headers) {
        return new HeaderWrapper(protocol, headers);
    }
    
    /**
     * @param headerSeparator 分隔符
     * @param headerPadding   填充符
     * @param protocol        协议
     * 
     * @return {@link HeaderWrapper}
     */
    public static final HeaderWrapper newBuilder(String headerSeparator, String headerPadding, String protocol) {
        return new HeaderWrapper(headerSeparator, headerPadding, protocol, new LinkedHashMap<String, List<String>>());
    }
    
    /**
     * @param headerSeparator 分隔符
     * @param headerPadding   填充符
     * @param protocol        协议
     * @param headers         头部信息
     * 
     * @return {@link HeaderWrapper}
     */
    public static final HeaderWrapper newBuilder(String headerSeparator, String headerPadding, String protocol, Map<String, List<String>> headers) {
        return new HeaderWrapper(headerSeparator, headerPadding, protocol, headers);
    }
    
    /**
     * 解析协议
     * 
     * @param lines 头部信息
     * 
     * @return 协议
     */
    private String buildProtocol(String[] lines) {
        if(ArrayUtils.isEmpty(lines)) {
            return null;
        } else {
            final String firstLine = lines[0];
            if(
                firstLine == null ||
                firstLine.indexOf(this.headerSeparator) >= 0
            ) {
                return null;
            } else {
                return firstLine.strip();
            }
        }
    }
    
    /**
     * 解析头部信息
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
        // 是否含有协议
        for (int jndex = this.hasProtocol ? 1 : 0; jndex < lines.length; jndex++) {
            line = lines[jndex];
            if(StringUtils.isEmpty(line)) {
                continue;
            }
            index = line.indexOf(this.headerSeparator);
            if(index < 0) {
                key = line.strip();
                value = "";
            } else {
                key   = line.substring(0, index).strip();
                value = line.substring(index + 1).strip();
            }
            this.setHeader(key, value);
        }
    }
    
    /**
     * @return 协议
     */
    public String getProtocol() {
        return this.protocol;
    }
    
    /**
     * @param key 头部名称（忽略大小写）
     * 
     * @return 首个头部信息
     */
    public String getHeader(String key) {
        final List<String> list = this.getHeaderList(key);
        if(CollectionUtils.isEmpty(list)) {
            return null;
        }
        final String value = list.get(0);
        return value == null ? value : value.strip();
    }
    
    /**
     * @param key 头部名称（忽略大小写）
     * 
     * @return 头部信息集合
     */
    public List<String> getHeaderList(String key) {
        if(this.isEmpty()) {
            return List.of();
        }
        return this.headers.entrySet().stream()
            .filter(entry -> StringUtils.equalsIgnoreCase(key, entry.getKey()))
            .map(Entry::getValue)
            // 需要判断是否为空：空值转换异常
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(List.of());
    }

    /**
     * @param key   名称
     * @param value 信息
     * 
     * @return this
     */
    public HeaderWrapper setHeader(String key, String value) {
        Objects.requireNonNull(this.headers, "头部信息未初始化");
        this.headers.computeIfAbsent(key, newKey -> new ArrayList<>()).add(value);
        return this;
    }
    
    /**
     * @param key   名称
     * @param value 信息
     * 
     * @return this
     */
    public HeaderWrapper setHeader(String key, List<String> value) {
        Objects.requireNonNull(this.headers, "头部信息未初始化");
        this.headers.put(key, value);
        return this;
    }
    
    /**
     * @return 所有头部信息
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }
    
    /**
     * @return 头部信息文本
     */
    public String build() {
        final StringBuilder builder = new StringBuilder();
        if(this.hasProtocol) {
            builder
            .append(this.protocol)
            .append(HEADER_LINE_WRITER);
        }
        if(this.isEmpty()) {
            return builder.toString();
        }
        this.headers.forEach((key, list) -> {
            if(CollectionUtils.isEmpty(list)) {
                this.buildLine(builder, key, null);
            } else {
                list.stream()
                .forEach(value -> this.buildLine(builder, key, value));
            }
        });
        return builder.toString();
    }
    
    /**
     * @param builder 头部文本
     * @param key     头部名称
     * @param value   头部信息
     */
    protected void buildLine(StringBuilder builder, String key, String value) {
        builder
        .append(key)
        .append(this.headerSeparator)
        .append(this.headerPadding)
        .append(value == null ? "" : value.strip())
        .append(HEADER_LINE_WRITER);
    }
    
    /**
     * @return 是否为空
     */
    public boolean isEmpty() {
        return MapUtils.isEmpty(this.headers);
    }
    
    /**
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
