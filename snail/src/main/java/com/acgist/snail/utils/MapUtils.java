package com.acgist.snail.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.acgist.snail.config.SymbolConfig;

/**
 * Map工具
 * 
 * @author acgist
 */
public class MapUtils {

    private MapUtils() {
    }
    
    /**
     * @param map Map
     * 
     * @return 是否为空
     */
    public static final boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * @param map Map
     * 
     * @return 是否非空
     */
    public static final boolean isNotEmpty(Map<?, ?> map) {
        return !MapUtils.isEmpty(map);
    }
    
    /**
     * URL参数转为Map
     * 
     * @param query URL参数
     * 
     * @return Map
     */
    public static final Map<String, String> ofUrlQuery(String query) {
        if(query == null) {
            return Map.of();
        }
        final char equals = SymbolConfig.Symbol.EQUALS.toChar();
        return Stream.of(SymbolConfig.Symbol.AND.split(query))
            .map(v -> {
                final int index = v.indexOf(equals);
                if(index < 0) {
                    return new String[] { v, "" };
                } else {
                    return new String[] { v.substring(0, index), UrlUtils.decode(v.substring(index + 1)) };
                }
            })
            .collect(Collectors.toMap(v -> v[0], v -> v[1]));
    }
    
    /**
     * Map转为URL参数
     * 
     * @param map Map
     * 
     * @return URL参数
     */
    public static final String toUrlQuery(Map<String, String> map) {
        if(MapUtils.isEmpty(map)) {
            return null;
        }
        return map.entrySet().stream()
            .map(entry -> SymbolConfig.Symbol.EQUALS.join(entry.getKey(), UrlUtils.encode(entry.getValue())))
            .collect(Collectors.joining(SymbolConfig.Symbol.AND.toString()));
    }
    
    /**
     * @param map 数据
     * @param key 键
     * 
     * @return 对象
     */
    public static final Object get(Map<?, ?> map, String key) {
        if(map == null) {
            return null;
        }
        return map.get(key);
    }
    
    /**
     * @param map 数据
     * @param key 键
     * 
     * @return 字节
     */
    public static final Byte getByte(Map<?, ?> map, String key) {
        final Long value = MapUtils.getLong(map, key);
        if(value == null) {
            return null;
        }
        return value.byteValue();
    }
    
    /**
     * @param map 数据
     * @param key 键
     * 
     * @return 数值
     */
    public static final Integer getInteger(Map<?, ?> map, String key) {
        final Long value = MapUtils.getLong(map, key);
        if(value == null) {
            return null;
        }
        return value.intValue();
    }
    
    /**
     * @param map 数据
     * @param key 键
     * 
     * @return 数值
     */
    public static final Long getLong(Map<?, ?> map, String key) {
        if(map == null) {
            return null;
        }
        return (Long) map.get(key);
    }
    
    /**
     * @param map 数据
     * @param key 键
     * 
     * @return 字符串
     */
    public static final String getString(Map<?, ?> map, String key) {
        return MapUtils.getString(map, key, null);
    }
    
    /**
     * @param map      数据
     * @param key      键
     * @param encoding 编码
     * 
     * @return 字符串
     */
    public static final String getString(Map<?, ?> map, String key, String encoding) {
        final var bytes = MapUtils.getBytes(map, key);
        if(bytes == null) {
            return null;
        }
        return StringUtils.getCharsetString(bytes, encoding);
    }
    
    /**
     * @param map 数据
     * @param key 键
     * 
     * @return 字节数组
     */
    public static final byte[] getBytes(Map<?, ?> map, String key) {
        if(map == null) {
            return null;
        }
        return (byte[]) map.get(key);
    }
    
    /**
     * @param map 数据
     * @param key 键
     * 
     * @return 集合
     */
    public static final List<Object> getList(Map<?, ?> map, String key) {
        if(map == null) {
            return List.of();
        }
        final var result = (List<?>) map.get(key);
        if(result == null) {
            return List.of();
        }
        return result.stream()
            .collect(Collectors.toList());
    }
    
    /**
     * @param map 数据
     * @param key 键
     * 
     * @return Map
     */
    public static final Map<String, Object> getMap(Map<?, ?> map, String key) {
        if(map == null) {
            return Map.of();
        }
        final var result = (Map<?, ?>) map.get(key);
        if(result == null) {
            return Map.of();
        }
        // 使用LinkedHashMap防止乱序
        return result.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> (String) entry.getKey(),
//              entry -> entry.getKey() == null ? null : entry.getKey().toString(),
                Map.Entry::getValue,
                (a, b) -> b,
                LinkedHashMap::new
            ));
    }
    
}
