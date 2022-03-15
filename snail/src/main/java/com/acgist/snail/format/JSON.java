package com.acgist.snail.format;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>JSON工具</p>
 * 
 * @author acgist
 */
public final class JSON {

	/**
	 * <p>JSON特殊字符</p>
	 * <p>Chrome浏览器控制台执行以下代码获取特殊字符：</p>
	 * <pre>
	 * var array = {};
	 * for (var i = 0, value = '', array = []; i < 0xFFFF; i++) {
	 * 	if(i >= 0xD800 && i <= 0xDFFF) {
	 * 		continue;
	 * 	}
	 * 	value = JSON.stringify(String.fromCharCode(i));
	 * 	value.indexOf("\\") > -1 && array.push(value);
	 * }
	 * console.log(array.join(", "));
	 * </pre>
	 * <p>其他特殊字符（不处理）：D800~DFFF</p>
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
	 * <p>特殊字符对应编码</p>
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
	 * <p>转义字符串：{@value}</p>
	 */
	private static final char JSON_ESCAPE = '\\';
	/**
	 * <p>{@link Map}前缀：{@value}</p>
	 */
	private static final char JSON_MAP_PREFIX = '{';
	/**
	 * <p>{@link Map}后缀：{@value}</p>
	 */
	private static final char JSON_MAP_SUFFIX = '}';
	/**
	 * <p>{@link List}前缀：{@value}</p>
	 */
	private static final char JSON_LIST_PREFIX = '[';
	/**
	 * <p>{@link List}后缀：{@value}</p>
	 */
	private static final char JSON_LIST_SUFFIX = ']';
	/**
	 * <p>键值分隔符：{@value}</p>
	 */
	private static final char JSON_KV_SEPARATOR = ':';
	/**
	 * <p>属性分隔符：{@value}</p>
	 */
	private static final char JSON_ATTR_SEPARATOR = ',';
	/**
	 * <p>字符串：{@value}</p>
	 */
	private static final char JSON_STRING = '"';
	/**
	 * <p>空值：{@value}</p>
	 */
	private static final String JSON_NULL = "null";
	/**
	 * <p>boolean类型：{@value}</p>
	 */
	private static final String JSON_BOOLEAN_TRUE = "true";
	/**
	 * <p>boolean类型：{@value}</p>
	 */
	private static final String JSON_BOOLEAN_FALSE = "false";
	
	/**
	 * <p>JSON数据类型</p>
	 * 
	 * @author acgist
	 */
	public enum Type {

		/**
		 * <p>Map</p>
		 */
		MAP,
		/**
		 * <p>List</p>
		 */
		LIST;
		
	}
	
	/**
	 * <p>类型</p>
	 */
	private Type type;
	/**
	 * <p>List</p>
	 */
	private List<Object> list;
	/**
	 * <p>Map</p>
	 */
	private Map<Object, Object> map;
	/**
	 * <p>是否使用懒加载</p>
	 * <p>懒加载：反序列化JSON时不会立即解析所有的JSON对象</p>
	 */
	private static boolean lazy = true;
	
	private JSON() {
	}
	
	/**
	 * <p>使用Map生成JSON对象</p>
	 * 
	 * @param map {@link Map}
	 * 
	 * @return JSON对象
	 */
	public static final JSON ofMap(Map<?, ?> map) {
		final JSON json = new JSON();
		json.map = new LinkedHashMap<>(map);
		json.type = Type.MAP;
		return json;
	}
	
	/**
	 * <p>使用List生成JSON对象</p>
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
	 * <p>将字符串转为为JSON对象</p>
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
		final JSON json = new JSON();
		final char prefix = content.charAt(0);
		final char suffix = content.charAt(content.length() - 1);
		if(prefix == JSON_MAP_PREFIX && suffix == JSON_MAP_SUFFIX) {
			json.type = Type.MAP;
		} else if(prefix == JSON_LIST_PREFIX && suffix == JSON_LIST_SUFFIX) {
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
	 * <p>使用懒加载</p>
	 */
	public static final void lazy() {
		JSON.lazy = true;
	}
	
	/**
	 * <p>禁用懒加载</p>
	 */
	public static final void eager() {
		JSON.lazy = false;
	}
	
	/**
	 * <p>序列化JSON对象</p>
	 * 
	 * @return JSON字符串
	 */
	private String serialize() {
		final StringBuilder builder = new StringBuilder();
		if(this.type == Type.MAP) {
			serializeMap(this.map, builder);
		} else if(this.type == Type.LIST) {
			serializeList(this.list, builder);
		} else {
			throw new IllegalArgumentException("JSON类型错误：" + this.type);
		}
		return builder.toString();
	}

	/**
	 * <p>序列化Map</p>
	 * 
	 * @param map Map
	 * @param builder JSON字符串Builder
	 */
	private static final void serializeMap(Map<?, ?> map, StringBuilder builder) {
		Objects.requireNonNull(map, "JSON序列化Map失败");
		builder.append(JSON_MAP_PREFIX);
		if(!map.isEmpty()) {
			map.forEach((key, value) -> {
				serializeValue(key, builder);
				builder.append(JSON_KV_SEPARATOR);
				serializeValue(value, builder);
				builder.append(JSON_ATTR_SEPARATOR);
			});
			builder.setLength(builder.length() - 1);
		}
		builder.append(JSON_MAP_SUFFIX);
	}
	
	/**
	 * <p>序列化List</p>
	 * 
	 * @param list List
	 * @param builder JSON字符串Builder
	 */
	private static final void serializeList(List<?> list, StringBuilder builder) {
		Objects.requireNonNull(list, "JSON序列化List失败");
		builder.append(JSON_LIST_PREFIX);
		if(!list.isEmpty()) {
			list.forEach(value -> {
				serializeValue(value, builder);
				builder.append(JSON_ATTR_SEPARATOR);
			});
			builder.setLength(builder.length() - 1);
		}
		builder.append(JSON_LIST_SUFFIX);
	}
	
	/**
	 * <p>序列化Java对象</p>
	 * 
	 * @param object Java对象
	 * @param builder JSON字符串Builder
	 */
	private static final void serializeValue(Object object, StringBuilder builder) {
		if(object instanceof String string) {
			builder
				.append(JSON_STRING)
				.append(escapeValue(string))
				.append(JSON_STRING);
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
			builder.append(JSON_NULL);
		} else {
			builder
				.append(JSON_STRING)
				.append(escapeValue(object.toString()))
				.append(JSON_STRING);
		}
	}
	
	/**
	 * <p>反序列化JSON字符串</p>
	 * 
	 * @param content JSON字符串
	 */
	private void deserialize(String content) {
		if(this.type == Type.MAP) {
			this.map = new LinkedHashMap<>();
			deserializeMap(content, this.map);
		} else if(this.type == Type.LIST) {
			this.list = new ArrayList<>();
			deserializeList(content, this.list);
		} else {
			throw new IllegalArgumentException("JSON类型错误：" + this.type);
		}
	}
	
	/**
	 * <p>反序列化Map</p>
	 * 
	 * @param content JSON字符串
	 * @param map Map
	 */
	private static final void deserializeMap(String content, Map<Object, Object> map) {
		content = unescapeValue(content);
		final int length = content.length();
		final AtomicInteger index = new AtomicInteger(0);
		while(index.get() < length) {
			map.put(
				deserializeValue(index, content),
				deserializeValue(index, content)
			);
		}
	}
	
	/**
	 * <p>反序列化List</p>
	 * 
	 * @param content JSON字符串
	 * @param list List
	 */
	private static final void deserializeList(String content, List<Object> list) {
		content = unescapeValue(content);
		final int length = content.length();
		final AtomicInteger index = new AtomicInteger(0);
		while(index.get() < length) {
			list.add(
				deserializeValue(index, content)
			);
		}
	}
	
	/**
	 * <p>反序列化JSON字符串</p>
	 * 
	 * @param index 字符索引
	 * @param content JSON字符串
	 * 
	 * @return Java对象
	 */
	private static final Object deserializeValue(AtomicInteger index, String content) {
		char value;
		String hexValue;
		// JSON层级
		int jsonIndex = 0;
		// String层级
		int stringIndex = 0;
		final int length = content.length();
		final StringBuilder builder = new StringBuilder();
		do {
			value = content.charAt(index.get());
			if(value == JSON_STRING) {
				if(stringIndex == 0) {
					stringIndex++;
				} else {
					stringIndex--;
				}
			} else if(value == JSON_MAP_PREFIX || value == JSON_LIST_PREFIX) {
				jsonIndex++;
			} else if(value == JSON_MAP_SUFFIX || value == JSON_LIST_SUFFIX) {
				jsonIndex--;
			}
			// 结束循环
			if(stringIndex == 0 && jsonIndex == 0 && (value == JSON_KV_SEPARATOR || value == JSON_ATTR_SEPARATOR)) {
				index.incrementAndGet();
				break;
			}
			// 转义参考：#CHARS
			if (value == JSON_ESCAPE) {
				value = content.charAt(index.incrementAndGet());
				switch (value) {
					case 'b' -> builder.append('\b');
					case 't' -> builder.append('\t');
					case 'n' -> builder.append('\n');
					case 'f' -> builder.append('\f');
					case 'r' -> builder.append('\r');
					case '"', JSON_ESCAPE -> {
						// 如果存在JSON对象里面保留转义字符
						if(jsonIndex != 0) {
							builder.append(JSON_ESCAPE);
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
						builder.append(JSON_ESCAPE);
						builder.append(value);
					}
				}
			} else {
				builder.append(value);
			}
		} while (index.incrementAndGet() < length);
		return deserializeValue(builder.toString());
	}
	
	/**
	 * <p>类型转换</p>
	 * <p>注意顺序：优先判断等于，然后判断equals，最后判断数值（正则表达式）。</p>
	 * 
	 * @param content JSON字符串
	 * 
	 * @return Java对象
	 */
	private static final Object deserializeValue(String content) {
		final String value = content.strip();
		final int length = value.length();
		char first = '0';
		char last = '0';
		if(length > 1) {
			first = value.charAt(0);
			last = value.charAt(length - 1);
		}
		if(first == JSON_STRING && last == JSON_STRING) {
			return value.substring(1, length - 1);
		} else if(
			(first == JSON_MAP_PREFIX && last == JSON_MAP_SUFFIX) ||
			(first == JSON_LIST_PREFIX && last == JSON_LIST_SUFFIX)
		) {
			if(JSON.lazy) {
				return value;
			} else {
				return JSON.ofString(value);
			}
		} else if(JSON_BOOLEAN_TRUE.equals(value) || JSON_BOOLEAN_FALSE.equals(value)) {
			return Boolean.valueOf(value);
		} else if(JSON_NULL.equals(value)) {
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
	 * <p>转义JSON字符串</p>
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
			// #CHARS
			if(value > 0x1F && value != 0x22 && value != 0x5C) {
				builder.append(value);
			} else {
				builder.append(CHARS_ESCAPE[ArrayUtils.indexOf(CHARS, value)]);
			}
		}
		return builder;
	}
	
	/**
	 * <p>反转义JSON字符串</p>
	 * 
	 * @param content 转义字符串
	 * 
	 * @return 原始字符串
	 * 
	 * @see #CHARS
	 * @see #CHARS_ESCAPE
	 */
	private static final String unescapeValue(String content) {
		if(content.charAt(0) == JSON_ESCAPE) {
			for (int index = 0; index < CHARS_ESCAPE.length; index++) {
				content = content.replace(CHARS_ESCAPE[index], String.valueOf(CHARS[index]));
			}
		}
		return content;
	}
	
	/**
	 * <p>获取Map</p>
	 * 
	 * @return Map
	 */
	public Map<Object, Object> getMap() {
		return this.map;
	}
	
	/**
	 * <p>获取List</p>
	 * 
	 * @return List
	 */
	public List<Object> getList() {
		return this.list;
	}
	
	/**
	 * <p>获取JSON对象</p>
	 * 
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
	 * <p>获取Integer属性对象</p>
	 * 
	 * @param key 属性名称
	 * 
	 * @return Integer
	 */
	public Integer getInteger(Object key) {
		return (Integer) this.get(key);
	}

	/**
	 * <p>获取Boolean属性对象</p>
	 * 
	 * @param key 属性名称
	 * 
	 * @return Boolean
	 */
	public Boolean getBoolean(Object key) {
		return (Boolean) this.get(key);
	}
	
	/**
	 * <p>获取String属性对象</p>
	 * 
	 * @param key 属性名称
	 * 
	 * @return String
	 */
	public String getString(Object key) {
		return (String) this.get(key);
	}
	
	/**
	 * <p>获取属性对象</p>
	 * 
	 * @param key 属性名称
	 * 
	 * @return 属性对象
	 */
	public Object get(Object key) {
		return this.map.get(key);
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
