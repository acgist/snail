package com.acgist.snail.context.wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.utils.StringUtils;

/**
 * Key-Value包装器
 * 
 * @author acgist
 */
public final class KeyValueWrapper {

    /**
     * 默认separator
     * 
     * @see #separator
     */
    private static final char DEFAULT_SEPARATOR = SymbolConfig.Symbol.AND.toChar();
    /**
     * 默认kvSeparator
     * 
     * @see #kvSeparator
     */
    private static final char DEFAULT_KV_SEPARATOR = SymbolConfig.Symbol.EQUALS.toChar();

    /**
     * 连接符
     */
    private final char separator;
    /**
     * Key-Value连接符
     */
    private final char kvSeparator;
    /**
     * 编码数据
     */
    private final String content;
    /**
     * 解码数据
     */
    private final Map<String, String> data;

    /**
     * @param separator   连接符
     * @param kvSeparator Key-Value连接符
     * @param content     编码数据
     * @param data        解码数据
     */
    private KeyValueWrapper(char separator, char kvSeparator, String content, Map<String, String> data) {
        this.separator   = separator;
        this.kvSeparator = kvSeparator;
        this.content     = content;
        if(data == null) {
            this.data = new HashMap<>();
        } else {
            this.data = data;
        }
    }

    /**
     * @return {@link KeyValueWrapper}
     */
    public static final KeyValueWrapper newInstance() {
        return new KeyValueWrapper(DEFAULT_SEPARATOR, DEFAULT_KV_SEPARATOR, null, null);
    }
    
    /**
     * @param content 编码数据
     * 
     * @return {@link KeyValueWrapper}
     */
    public static final KeyValueWrapper newInstance(String content) {
        return new KeyValueWrapper(DEFAULT_SEPARATOR, DEFAULT_KV_SEPARATOR, content, null);
    }
    
    /**
     * @param data 解码数据
     * 
     * @return {@link KeyValueWrapper}
     */
    public static final KeyValueWrapper newInstance(Map<String, String> data) {
        return new KeyValueWrapper(DEFAULT_SEPARATOR, DEFAULT_KV_SEPARATOR, null, data);
    }
    
    /**
     * @param separator   连接符
     * @param kvSeparator Key-Value连接符
     * 
     * @return {@link KeyValueWrapper}
     */
    public static final KeyValueWrapper newInstance(char separator, char kvSeparator) {
        return new KeyValueWrapper(separator, kvSeparator, null, null);
    }
    
    /**
     * @param separator   连接符
     * @param kvSeparator Key-Value连接符
     * @param content     编码数据
     * 
     * @return {@link KeyValueWrapper}
     */
    public static final KeyValueWrapper newInstance(char separator, char kvSeparator, String content) {
        return new KeyValueWrapper(separator, kvSeparator, content, null);
    }
    
    /**
     * @param separator   连接符
     * @param kvSeparator Key-Value连接符
     * @param data        解码数据
     * 
     * @return {@link KeyValueWrapper}
     */
    public static final KeyValueWrapper newInstance(char separator, char kvSeparator, Map<String, String> data) {
        return new KeyValueWrapper(separator, kvSeparator, null, data);
    }
    
    /**
     * 数据编码
     * 
     * @return 编码数据
     */
    public String encode() {
        final StringBuilder builder = new StringBuilder();
        this.data.forEach(
            (key, value)
            ->
            builder
            .append(key)
            .append(this.kvSeparator)
            .append(value)
            .append(this.separator)
        );
        final int length = builder.length();
        if(length > 0) {
            builder.setLength(length - 1);
        }
        return builder.toString();
    }

    /**
     * 数据解码
     * 
     * @return this
     */
    public KeyValueWrapper decode() {
        if(StringUtils.isEmpty(this.content)) {
            return this;
        }
        int left = 0;
        int pos  = -1;
        int index;
        String key;
        String value;
        String keyValue;
        final int length = this.content.length();
        do {
            left = pos + 1;
//          left = pos + Character.toString(this.separator).length();
            pos = this.content.indexOf(this.separator, left);
            if(pos < 0) {
                keyValue = this.content.substring(left).strip();
            } else {
                keyValue = this.content.substring(left, pos).strip();
            }
            if(keyValue.isEmpty()) {
                continue;
            }
            index = keyValue.indexOf(this.kvSeparator);
            if(index < 0) {
                key = keyValue.strip();
                value = "";
            } else {
                key = keyValue.substring(0, index).strip();
                value = keyValue.substring(index + 1).strip();
            }
            this.data.put(key, value);
        } while(0 <= pos && pos < length);
        return this;
    }
    
    /**
     * @param key Key
     * 
     * @return Value
     */
    public String get(String key) {
        return this.get(key, null);
    }
    
    /**
     * @param key          Key
     * @param defaultValue 默认值
     * 
     * @return Value
     */
    public String get(String key, String defaultValue) {
        return this.data.getOrDefault(key, defaultValue);
    }
    
    /**
     * @param key Key（忽略大小写）
     * 
     * @return Value
     */
    public String getIgnoreCase(String key) {
        return this.getIgnoreCase(key, null);
    }

    /**
     * @param key          Key（忽略大小写）
     * @param defaultValue 默认值
     * 
     * @return Value
     */
    public String getIgnoreCase(String key, String defaultValue) {
        return this.data.entrySet().stream()
            .filter(entry -> StringUtils.equalsIgnoreCase(entry.getKey(), key))
            .map(Entry::getValue)
            // 需要判断是否为空：空值转换异常
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(defaultValue);
    }
    
    @Override
    public String toString() {
        return this.data == null ? null : this.data.toString();
    }
    
}
