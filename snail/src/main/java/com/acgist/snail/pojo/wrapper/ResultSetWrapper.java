package com.acgist.snail.pojo.wrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.acgist.snail.utils.StringUtils;

/**
 * <p>结果集包装器</p>
 * 
 * @author acgist
 */
public final class ResultSetWrapper {

	/**
	 * <p>结果集数据</p>
	 * <p>字段名称=字段值</p>
	 */
	private final Map<String, Object> data = new HashMap<>();
	
	/**
	 * <p>设置结果集数据</p>
	 * 
	 * @param key 字段名称
	 * @param value 字段值
	 */
	public void put(String key, Object value) {
		this.data.put(key, value);
	}
	
	/**
	 * <p>获取String</p>
	 * 
	 * @param key 字段名称
	 * 
	 * @return String
	 */
	public String getString(String key) {
		return (String) this.get(key);
	}
	
	/**
	 * <p>获取Integer</p>
	 * 
	 * @param key 字段名称
	 * 
	 * @return Integer
	 */
	public Integer getInteger(String key) {
		return (Integer) this.get(key);
	}

	/**
	 * <p>获取Long</p>
	 * 
	 * @param key 字段名称
	 * 
	 * @return Long
	 */
	public Long getLong(String key) {
		return (Long) this.get(key);
	}

	/**
	 * <p>获取Date</p>
	 * 
	 * @param key 字段名称
	 * 
	 * @return Date
	 */
	public Date getDate(String key) {
		return (Date) this.get(key);
	}

	/**
	 * <p>获取对象</p>
	 * 
	 * @param key 字段名称
	 * 
	 * @return 对象
	 */
	public Object get(String key) {
		Objects.requireNonNull(key, "字段名称为空");
		return this.data.get(key);
	}
	
	/**
	 * <p>获取对象（忽略大小写）</p>
	 * 
	 * @param key 字段名称
	 * 
	 * @return 对象
	 */
	public Object getIgnoreCase(String key) {
		Objects.requireNonNull(key, "字段名称为空");
		return this.data.entrySet().stream()
			.filter(entry -> StringUtils.equalsIgnoreCase(entry.getKey(), key))
			.map(Entry::getValue)
			.findFirst()
			.orElse(null);
	}

	@Override
	public String toString() {
		return this.data.toString();
	}
	
}
