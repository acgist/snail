package com.acgist.snail.pojo.wrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>数据库结果集包装器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ResultSetWrapper {

	/**
	 * <p>结果集Map</p>
	 */
	private final Map<String, Object> data = new HashMap<>();
	
	/**
	 * <p>设置结果集</p>
	 * 
	 * @param key 字段名称
	 * @param value 字段值
	 */
	public void put(String key, Object value) {
		this.data.put(key.toUpperCase(), value);
	}
	
	/**
	 * @return 字符串
	 */
	public String getString(String key) {
		return (String) getObject(key);
	}
	
	/**
	 * @return Integer
	 */
	public Integer getInteger(String key) {
		return (Integer) getObject(key);
	}

	/**
	 * @return Long
	 */
	public Long getLong(String key) {
		return (Long) getObject(key);
	}

	/**
	 * @return 日期
	 */
	public Date getDate(String key) {
		return (Date) getObject(key);
	}

	/**
	 * @return 对象
	 */
	public Object getObject(String key) {
		return this.data.get(key.toUpperCase());
	}

	@Override
	public String toString() {
		return this.data.toString();
	}
	
}
