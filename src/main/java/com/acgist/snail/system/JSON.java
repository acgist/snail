package com.acgist.snail.system;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>JSON处理工具</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public class JSON {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSON.class);
	
	/**
	 * <p>特殊字符</p>
	 * <p>Chrome浏览器控制台执行：</p>
	 * <pre>
for (var i = 0, value = '', array = []; i < 0xFFFF; i++) {
    value = JSON.stringify(String.fromCharCode(i));
    value.indexOf("\\") > -1 && array.push(value);
}
console.log(array.join(", "));
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
	 * 特殊字符对应编码
	 */
	private static final String[] CHARS_ENCODE = new String[] {
		"\\u0000", "\\u0001", "\\u0002", "\\u0003", "\\u0004", "\\u0005",
		"\\u0006", "\\u0007", "\\b", "\\t", "\\n", "\\u000b", "\\f", "\\r",
		"\\u000e", "\\u000f", "\\u0010", "\\u0011", "\\u0012", "\\u0013",
		"\\u0014", "\\u0015", "\\u0016", "\\u0017", "\\u0018", "\\u0019",
		"\\u001a", "\\u001b", "\\u001c", "\\u001d", "\\u001e", "\\u001f",
		"\\\"", "\\\\"
	};
	/**
	 * Map前缀
	 */
	private static final char JSON_MAP_PREFIX = '{';
	/**
	 * Map后缀
	 */
	private static final char JSON_MAP_SUFFIX = '}';
	/**
	 * List前缀
	 */
	private static final char JSON_LIST_PREFIX = '[';
	/**
	 * List后缀
	 */
	private static final char JSON_LIST_SUFFIX = ']';
	/**
	 * JSON：键值分隔符
	 */
	private static final char JSON_KV = ':';
	/**
	 * JSON：属性分隔符
	 */
	private static final char JSON_ATTR = ',';
	/**
	 * JSON：字符串
	 */
	private static final char JSON_STRING = '"';
	/**
	 * JSON：boolean：true
	 */
	private static final String JSON_BOOLEAN_TRUE = "true";
	/**
	 * JSON：boolean：false
	 */
	private static final String JSON_BOOLEAN_FALSE = "false";
	/**
	 * JSON：null
	 */
	private static final String JSON_NULL = "null";
	
	/**
	 * 类型
	 */
	public enum Type {

		/** map */
		map,
		/** list */
		list;
		
	}
	
	private Type type;
	private Map<Object, Object> map;
	private List<Object> list;
	
	private JSON() {
	}
	
	public static final JSON ofMap(Map<Object, Object> map) {
		final JSON json = new JSON();
		json.map = map;
		json.type = Type.map;
		return json;
	}
	
	public static final JSON ofList(List<Object> list) {
		final JSON json = new JSON();
		json.list = list;
		json.type = Type.list;
		return json;
	}

	public static final JSON ofString(String content) {
		if(StringUtils.isEmpty(content)) {
			throw new ArgumentException("JSON格式错误：" + content);
		}
		content = content.trim();
		final JSON json = new JSON();
		final char prefix = content.charAt(0);
		final char suffix = content.charAt(content.length() - 1);
		if(prefix == JSON_MAP_PREFIX && suffix == JSON_MAP_SUFFIX) {
			json.type = Type.map;
		} else if(prefix == JSON_LIST_PREFIX && suffix == JSON_LIST_SUFFIX) {
			json.type = Type.list;
		} else {
			throw new ArgumentException("JSON格式错误（类型）：" + content);
		}
		content = content.substring(1, content.length() - 1); // 去掉首位字符
		json.deserialize(content);
		return json;
	}
	
	/**
	 * 序列化
	 */
	private String serialize() {
		final StringBuilder builder = new StringBuilder();
		if(this.type == Type.map) {
			this.serializeMap(this.map, builder);
		} else if(this.type == Type.list) {
			this.serializeList(this.list, builder);
		} else {
			throw new ArgumentException("JSON类型错误：" + this.type);
		}
		return builder.toString();
	}

	private void serializeMap(Map<?, ?> map, StringBuilder builder) {
		if(map == null) {
			throw new ArgumentException("JSON序列化错误：Map=null");
		}
		builder.append(JSON_MAP_PREFIX);
		if(!map.isEmpty()) {
			map.entrySet().forEach(entry -> {
				serializeValue(entry.getKey(), builder);
				builder.append(JSON_KV);
				serializeValue(entry.getValue(), builder);
				builder.append(JSON_ATTR);
			});
			builder.setLength(builder.length() - 1);
		}
		builder.append(JSON_MAP_SUFFIX);
	}
	
	private void serializeList(List<?> list, StringBuilder builder) {
		if(list == null) {
			throw new ArgumentException("JSON序列化错误：List=null");
		}
		builder.append(JSON_LIST_PREFIX);
		if(!list.isEmpty()) {
			list.forEach(value -> {
				serializeValue(value, builder);
				builder.append(JSON_ATTR);
			});
			builder.setLength(builder.length() - 1);
		}
		builder.append(JSON_LIST_SUFFIX);
	}
	
	/**
	 * 序列化JSON属性
	 */
	private void serializeValue(Object object, StringBuilder builder) {
		if(object == null) {
			builder.append(JSON_NULL);
		} else if(object instanceof String) {
			builder.append(JSON_STRING).append(serializeValue((String) object)).append(JSON_STRING);
		} else if(object instanceof Boolean) {
			builder.append(object.toString());
		} else if(object instanceof Number) {
			builder.append(object.toString());
		} else if(object instanceof JSON) {
			builder.append(object.toString());
		} else if(object instanceof Map) {
			serializeMap((Map<?, ?>) object, builder);
		} else if(object instanceof List) {
			serializeList((List<?>) object, builder);
		} else {
			builder.append(object.toString());
		}
	}
	
	/**
	 * 转义字符
	 */
	private String serializeValue(String content) {
		int index = -1;
		final char[] chars = content.toCharArray();
		final StringBuilder builder = new StringBuilder();
		for (char value : chars) {
			index = ArrayUtils.indexOf(CHARS, value);
			if(index == -1) {
				builder.append(value);
			} else {
				builder.append(CHARS_ENCODE[index]);
			}
		}
		return builder.toString();
	}
	
	/**
	 * 反序列化
	 */
	private void deserialize(String content) {
		if(this.type == Type.map) {
			deserializeMap(content);
		} else if(this.type == Type.list) {
			deserializeList(content);
		} else {
			throw new ArgumentException("JSON类型错误：" + this.type);
		}
	}
	
	private void deserializeMap(String content) {
		this.map = new LinkedHashMap<>();
		final AtomicInteger index = new AtomicInteger(0);
		while(index.get() < content.length()) {
			this.map.put(
				deserializeValue(index, content),
				deserializeValue(index, content)
			);
		}
	}
	
	private void deserializeList(String content) {
		this.list = new ArrayList<>();
		final AtomicInteger index = new AtomicInteger(0);
		while(index.get() < content.length()) {
			this.list.add(
				deserializeValue(index, content)
			);
		}
	}
	
	/**
	 * 解析JSON属性
	 */
	private Object deserializeValue(AtomicInteger index, String content) {
		char value;
		boolean string = false; // 是否是字符串对象
		boolean json = false; // 是否是JSON对象
		String hexValue;
		final StringBuilder builder = new StringBuilder();
		do {
			value = content.charAt(index.get());
			if(value == JSON_STRING) {
				if(string) {
					string = false;
				} else {
					string = true;
				}
			} else if(value == JSON_MAP_PREFIX || value == JSON_LIST_PREFIX) {
				json = true;
			} else if(value == JSON_MAP_SUFFIX || value == JSON_LIST_SUFFIX) {
				json = false;
			}
			// 不属于JSON对象和字符串对象出现分隔符：结束循环
			if(!string && !json && (value == JSON_KV || value == JSON_ATTR)) {
				index.incrementAndGet();
				break;
			}
			if (value == '\\') { // 转义：参考{@link #BYTES}
				index.incrementAndGet();
				value = content.charAt(index.get());
				switch (value) {
				case 'b':
					builder.append('\b');
					break;
				case 't':
					builder.append('\t');
					break;
				case 'n':
					builder.append('\n');
					break;
				case 'f':
					builder.append('\f');
					break;
				case 'r':
					builder.append('\r');
					break;
				case '"':
					builder.append(value);
					break;
				case '\\':
					builder.append(value);
					break;
				case 'u': // Unicode
					hexValue = content.substring(index.get() + 1, index.get() + 5);
					builder.append((char) Integer.parseInt(hexValue, 16));
					index.addAndGet(4);
					break;
				default:
					builder.append(value);
					LOGGER.warn("不支持的JSON转义符号：{}", value);
					break;
				}
			} else {
				builder.append(value);
			}
		} while (index.incrementAndGet() < content.length());
		return convertValue(builder.toString());
	}
	
	/**
	 * 类型转换
	 */
	private Object convertValue(String content) {
		final String value = content.trim();
		final int length = value.length();
		if(
			length > 1 &&
			value.charAt(0) == JSON_STRING &&
			value.charAt(value.length() - 1) == JSON_STRING
		) { // 字符串
			return value.substring(1, length - 1); // 去掉引号
		} else if(
			JSON_BOOLEAN_TRUE.equals(value) ||
			JSON_BOOLEAN_FALSE.equals(value)
		) { // Boolean
			return Boolean.valueOf(value);
		} else if(JSON_NULL.equals(value)) { // null
			return null;
		} else if(StringUtils.isDecimal(value)) { // 数字
			return Integer.valueOf(value);
		} else if(
			length > 1 &&
			value.charAt(0) == JSON_MAP_PREFIX && value.charAt(length - 1) == JSON_MAP_SUFFIX
		) { // MAP：懒加载
//			return JSON.ofString(object);
			return value;
		} else if(
			length > 1 &&
			value.charAt(0) == JSON_LIST_PREFIX && value.charAt(length - 1) == JSON_LIST_SUFFIX
		) { // LIST：懒加载
//			return JSON.ofString(object);
			return value;
		} else {
			throw new ArgumentException("JSON格式错误：" + value);
		}
	}
	
	public JSON getJSON(Object key) {
		final String value = getString(key);
		if(value == null) {
			return null;
		} else {
			return JSON.ofString(value);
		}
	}
	
	public List<Object> getList() {
		return this.list;
	}
	
	public Integer getInteger(Object key) {
		return (Integer) this.get(key);
	}

	public Boolean getBoolean(Object key) {
		return (Boolean) this.get(key);
	}
	
	public String getString(Object key) {
		return (String) this.get(key);
	}
	
	public Object get(Object key) {
		return this.map.get(key);
	}
	
	public String toJSON() {
		return this.serialize();
	}
	
	@Override
	public String toString() {
		return this.serialize();
	}
	
}
