package com.acgist.snail.format;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * JSON工具
 * 
 * 数字默认类型：Long
 * 支持数据类型：JSON、Number、String、Boolean、Map、List
 * 
 * @author acgist
 */
public final class JSON {

    /**
     * 特殊字符需要转义
     * Chrome浏览器控制台执行以下代码获取特殊字符：
     * <pre>
     * let array = {};
     * for (let i = 0, value = '', array = []; i < 0xFFFF; i++) {
     *     // 其他特殊字符跳过：D800~DFFF
     *     if(i >= 0xD800 && i <= 0xDFFF) {
     *         continue;
     *     }
     *     value = JSON.stringify(String.fromCharCode(i));
     *     value.indexOf("\\") > -1 && array.push(value);
     * }
     * console.info(array.join(", "));
     * </pre>
     */
    private static final char[] CHARS = new char[] {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
        '\u0006', '\u0007', '\b', '\t', '\n', '\u000b', '\f', '\r',
        '\u000e', '\u000f', '\u0010', '\u0011', '\u0012', '\u0013',
        '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019',
        '\u001a', '\u001b', '\u001c', '\u001d', '\u001e', '\u001f',
        '\"', '\\'
    };
    /**
     * 特殊字符对应编码
     */
    private static final String[] CHARS_ESCAPE = new String[] {
        "\\u0000", "\\u0001", "\\u0002", "\\u0003", "\\u0004", "\\u0005",
        "\\u0006", "\\u0007", "\\b", "\\t", "\\n", "\\u000b", "\\f", "\\r",
        "\\u000e", "\\u000f", "\\u0010", "\\u0011", "\\u0012", "\\u0013",
        "\\u0014", "\\u0015", "\\u0016", "\\u0017", "\\u0018", "\\u0019",
        "\\u001a", "\\u001b", "\\u001c", "\\u001d", "\\u001e", "\\u001f",
        "\\\"", "\\\\"
    };
    
    /**
     * JSON数据类型
     * 
     * @author acgist
     */
    public enum Type {

        /**
         * Map
         */
        MAP,
        /**
         * List
         */
        LIST;
        
    }
    
    /**
     * 类型
     */
    private Type type;
    /**
     * List
     */
    private List<Object> list;
    /**
     * Map
     */
    private Map<Object, Object> map;
    /**
     * 是否使用懒加载
     * 反序列化JSON时不会立即解析所有的JSON对象
     */
    private static boolean lazy = true;
    
    private JSON() {
    }
    
    /**
     * 使用Map生成JSON对象
     * 
     * @param map {@link Map}
     * 
     * @return JSON对象
     */
    public static final JSON ofMap(Map<?, ?> map) {
        final JSON json = new JSON();
        json.map  = new LinkedHashMap<>(map);
        json.type = Type.MAP;
        return json;
    }
    
    /**
     * 使用List生成JSON对象
     * 
     * @param list {@link List}
     * 
     * @return JSON对象
     */
    public static final JSON ofList(List<?> list) {
        final JSON json = new JSON();
        json.list = new ArrayList<>(list);
        json.type = Type.LIST;
        return json;
    }
    
    /**
     * 将字符串转为为JSON对象
     * 
     * @param content 字符串
     * 
     * @return JSON对象
     */
    public static final JSON ofString(String content) {
        if(StringUtils.isEmpty(content)) {
            throw new IllegalArgumentException("JSON格式错误：" + content);
        }
        content = content.strip();
        if(content.isEmpty()) {
            throw new IllegalArgumentException("JSON格式错误：" + content);
        }
        final JSON json   = new JSON();
        final char prefix = content.charAt(0);
        final char suffix = content.charAt(content.length() - 1);
        if(
            prefix == SymbolConfig.JSON.JSON_MAP_PREFIX &&
            suffix == SymbolConfig.JSON.JSON_MAP_SUFFIX
        ) {
            json.type = Type.MAP;
        } else if(
            prefix == SymbolConfig.JSON.JSON_LIST_PREFIX &&
            suffix == SymbolConfig.JSON.JSON_LIST_SUFFIX
        ) {
            json.type = Type.LIST;
        } else {
            throw new IllegalArgumentException("JSON格式错误：" + content);
        }
        // 去掉首尾字符
        content = content.substring(1, content.length() - 1);
        json.deserialize(content);
        return json;
    }
    
    /**
     * 使用懒加载
     */
    public static final void lazy() {
        JSON.lazy = true;
    }
    
    /**
     * 禁用懒加载
     */
    public static final void eager() {
        JSON.lazy = false;
    }
    
    /**
     * 序列化JSON对象
     * 
     * @return JSON字符串
     */
    private String serialize() {
        final StringBuilder builder = new StringBuilder();
        if(this.type == Type.MAP) {
            JSON.serializeMap(this.map, builder);
        } else if(this.type == Type.LIST) {
            JSON.serializeList(this.list, builder);
        } else {
            throw new IllegalArgumentException("JSON类型错误：" + this.type);
        }
        return builder.toString();
    }

    /**
     * 序列化Map
     * 
     * @param map     Map
     * @param builder JSON字符串Builder
     */
    private static final void serializeMap(Map<?, ?> map, StringBuilder builder) {
        Objects.requireNonNull(map, "JSON序列化Map失败");
        builder.append(SymbolConfig.JSON.JSON_MAP_PREFIX);
        if(!map.isEmpty()) {
            map.forEach((key, value) -> {
                JSON.serializeValue(key, builder);
                builder.append(SymbolConfig.JSON.JSON_KV_SEPARATOR);
                JSON.serializeValue(value, builder);
                builder.append(SymbolConfig.JSON.JSON_ATTR_SEPARATOR);
            });
            builder.setLength(builder.length() - 1);
        }
        builder.append(SymbolConfig.JSON.JSON_MAP_SUFFIX);
    }
    
    /**
     * 序列化List
     * 
     * @param list    List
     * @param builder JSON字符串Builder
     */
    private static final void serializeList(List<?> list, StringBuilder builder) {
        Objects.requireNonNull(list, "JSON序列化List失败");
        builder.append(SymbolConfig.JSON.JSON_LIST_PREFIX);
        if(!list.isEmpty()) {
            list.forEach(value -> {
                JSON.serializeValue(value, builder);
                builder.append(SymbolConfig.JSON.JSON_ATTR_SEPARATOR);
            });
            builder.setLength(builder.length() - 1);
        }
        builder.append(SymbolConfig.JSON.JSON_LIST_SUFFIX);
    }
    
    /**
     * 序列化Java对象
     * 
     * @param object  Java对象
     * @param builder JSON字符串Builder
     */
    private static final void serializeValue(Object object, StringBuilder builder) {
        if(object instanceof String string) {
            builder
                .append(SymbolConfig.JSON.JSON_STRING)
                .append(JSON.escapeValue(string))
                .append(SymbolConfig.JSON.JSON_STRING);
        } else if(object instanceof Number) {
            builder.append(object.toString());
        } else if(object instanceof Boolean) {
            builder.append(object.toString());
        } else if(object instanceof JSON) {
            builder.append(object.toString());
        } else if(object instanceof Map<?, ?> map) {
            serializeMap(map, builder);
        } else if(object instanceof List<?> list) {
            serializeList(list, builder);
        } else if(object == null) {
            builder.append(SymbolConfig.JSON.JSON_NULL);
        } else {
            builder
                .append(SymbolConfig.JSON.JSON_STRING)
                .append(JSON.escapeValue(object.toString()))
                .append(SymbolConfig.JSON.JSON_STRING);
        }
    }
    
    /**
     * 反序列化JSON字符串
     * 
     * @param content JSON字符串
     */
    private void deserialize(String content) {
        if(this.type == Type.MAP) {
            this.map = new LinkedHashMap<>();
            JSON.deserializeMap(content, this.map);
        } else if(this.type == Type.LIST) {
            this.list = new ArrayList<>();
            JSON.deserializeList(content, this.list);
        } else {
            throw new IllegalArgumentException("JSON类型错误：" + this.type);
        }
    }
    
    /**
     * 反序列化Map
     * 
     * @param content JSON字符串
     * @param map     Map
     */
    private static final void deserializeMap(String content, Map<Object, Object> map) {
        content = JSON.unescapeValue(content);
        final int length = content.length();
        final AtomicInteger index = new AtomicInteger(0);
        while(index.get() < length) {
            map.put(
                JSON.deserializeValue(index, content),
                JSON.deserializeValue(index, content)
            );
        }
    }
    
    /**
     * 反序列化List
     * 
     * @param content JSON字符串
     * @param list    List
     */
    private static final void deserializeList(String content, List<Object> list) {
        content = JSON.unescapeValue(content);
        final int length = content.length();
        final AtomicInteger index = new AtomicInteger(0);
        while(index.get() < length) {
            list.add(
                JSON.deserializeValue(index, content)
            );
        }
    }
    
    /**
     * 反序列化JSON字符串
     * 
     * @param index   字符索引
     * @param content JSON字符串
     * 
     * @return Java对象
     */
    private static final Object deserializeValue(AtomicInteger index, String content) {
        char value;
        String hexValue;
        int jsonIndex    = 0;
        int stringIndex  = 0;
        final int length = content.length();
        final StringBuilder builder = new StringBuilder();
        do {
            value = content.charAt(index.get());
            if(value == SymbolConfig.JSON.JSON_STRING) {
                if(stringIndex == 0) {
                    stringIndex++;
                } else {
                    stringIndex--;
                }
            } else if(
                value == SymbolConfig.JSON.JSON_MAP_PREFIX ||
                value == SymbolConfig.JSON.JSON_LIST_PREFIX
            ) {
                jsonIndex++;
            } else if(
                value == SymbolConfig.JSON.JSON_MAP_SUFFIX ||
                value == SymbolConfig.JSON.JSON_LIST_SUFFIX
            ) {
                jsonIndex--;
            }
            // 结束循环
            if(
                jsonIndex   == 0 &&
                stringIndex == 0 &&
                (value == SymbolConfig.JSON.JSON_KV_SEPARATOR || value == SymbolConfig.JSON.JSON_ATTR_SEPARATOR)
            ) {
                index.incrementAndGet();
                break;
            }
            if (value == SymbolConfig.JSON.JSON_ESCAPE) {
                value = content.charAt(index.incrementAndGet());
                switch (value) {
                    case 'b' -> builder.append('\b');
                    case 't' -> builder.append('\t');
                    case 'n' -> builder.append('\n');
                    case 'f' -> builder.append('\f');
                    case 'r' -> builder.append('\r');
                    case '"', SymbolConfig.JSON.JSON_ESCAPE -> {
                        // 如果存在JSON对象里面保留转义字符
                        if(jsonIndex != 0) {
                            builder.append(SymbolConfig.JSON.JSON_ESCAPE);
                        }
                        builder.append(value);
                    }
                    case 'u' -> {
                        // Unicode
                        hexValue = content.substring(index.get() + 1, index.get() + 5);
                        builder.append((char) Integer.parseInt(hexValue, 16));
                        index.addAndGet(4);
                    }
                    default -> {
                        // 未知转义类型保留转义字符
                        builder.append(SymbolConfig.JSON.JSON_ESCAPE);
                        builder.append(value);
                    }
                }
            } else {
                builder.append(value);
            }
        } while (index.incrementAndGet() < length);
        return JSON.deserializeValue(builder.toString());
    }
    
    /**
     * 类型转换
     * 注意顺序：优先判断等于，然后判断equals，最后判断数值（正则表达式）。
     * 
     * @param content JSON字符串
     * 
     * @return Java对象
     */
    private static final Object deserializeValue(String content) {
        final String value = content.strip();
        final int length = value.length();
        char aChar = '0';
        char zChar = '0';
        if(length > 1) {
            aChar = value.charAt(0);
            zChar = value.charAt(length - 1);
        }
        if(
            aChar == SymbolConfig.JSON.JSON_STRING &&
            zChar == SymbolConfig.JSON.JSON_STRING
        ) {
            return value.substring(1, length - 1);
        } else if(
            (aChar == SymbolConfig.JSON.JSON_MAP_PREFIX  && zChar == SymbolConfig.JSON.JSON_MAP_SUFFIX) ||
            (aChar == SymbolConfig.JSON.JSON_LIST_PREFIX && zChar == SymbolConfig.JSON.JSON_LIST_SUFFIX)
        ) {
            if(JSON.lazy) {
                return value;
            } else {
                return JSON.ofString(value);
            }
        } else if(
            SymbolConfig.JSON.JSON_BOOLEAN_TRUE.equals(value) ||
            SymbolConfig.JSON.JSON_BOOLEAN_FALSE.equals(value)
        ) {
            return Boolean.valueOf(value);
        } else if(SymbolConfig.JSON.JSON_NULL.equals(value)) {
            return null;
        } else if(StringUtils.isNumeric(value)) {
            return Long.valueOf(value);
        } else if(StringUtils.isDecimal(value)) {
            return Double.valueOf(value);
        } else {
            throw new IllegalArgumentException("JSON格式错误：" + value);
        }
    }
    
    /**
     * 转义JSON字符串
     * 
     * @param content 原始字符串
     * 
     * @return 转义字符串
     * 
     * @see #CHARS
     * @see #CHARS_ESCAPE
     */
    private static final StringBuilder escapeValue(String content) {
        final char[] chars = content.toCharArray();
        final StringBuilder builder = new StringBuilder();
        for (char value : chars) {
            if(value > 0x1F && value != 0x22 && value != 0x5C) {
                builder.append(value);
            } else {
                builder.append(CHARS_ESCAPE[ArrayUtils.indexOf(CHARS, value)]);
            }
        }
        return builder;
    }
    
    /**
     * 反转义JSON字符串
     * 
     * @param content 转义字符串
     * 
     * @return 原始字符串
     * 
     * @see #CHARS
     * @see #CHARS_ESCAPE
     */
    private static final String unescapeValue(String content) {
        if(content.charAt(0) == SymbolConfig.JSON.JSON_ESCAPE) {
            for (int index = 0; index < CHARS_ESCAPE.length; index++) {
                content = content.replace(CHARS_ESCAPE[index], String.valueOf(CHARS[index]));
            }
        }
        return content;
    }
    
    /**
     * @return Map
     */
    public Map<Object, Object> getMap() {
        return this.map;
    }
    
    /**
     * @return List
     */
    public List<Object> getList() {
        return this.list;
    }
    
    /**
     * @param key 属性名称
     * 
     * @return JSON对象
     */
    public JSON getJSON(Object key) {
        final Object value = this.get(key);
        if(value == null) {
            return null;
        } else if(value instanceof JSON json) {
            return json;
        } else if(value instanceof String string) {
            return JSON.ofString(string);
        } else if(value instanceof Map<?, ?> map) {
            return JSON.ofMap(map);
        } else if(value instanceof List<?> list) {
            return JSON.ofList(list);
        } else {
            throw new IllegalArgumentException("JSON转换错误：" + value);
        }
    }
    
    /**
     * @param key 属性名称
     * 
     * @return Integer
     */
    public Integer getInteger(Object key) {
        return (Integer) this.get(key);
    }

    /**
     * @param key 属性名称
     * 
     * @return Boolean
     */
    public Boolean getBoolean(Object key) {
        return (Boolean) this.get(key);
    }
    
    /**
     * @param key 属性名称
     * 
     * @return String
     */
    public String getString(Object key) {
        return (String) this.get(key);
    }
    
    /**
     * @param key 属性名称
     * 
     * @return 属性对象
     */
    public Object get(Object key) {
        return this.map.get(key);
    }
    
    /**
     * @param index 索引
     * 
     * @return Integer
     */
    public Integer getInteger(int index) {
        return (Integer) this.get(index);
    }
    
    /**
     * @param index 索引
     * 
     * @return Boolean
     */
    public Boolean getBoolean(int index) {
        return (Boolean) this.get(index);
    }
    
    /**
     * @param index 索引
     * 
     * @return String
     */
    public String getString(int index) {
        return (String) this.get(index);
    }
    
    /**
     * @param index 索引
     * 
     * @return 对象
     */
    public Object get(int index) {
        return this.list.get(index);
    }
    
    /**
     * @return JSON字符串
     */
    public String toJSON() {
        return this.serialize();
    }
    
    @Override
    public String toString() {
        return this.serialize();
    }
    
}
