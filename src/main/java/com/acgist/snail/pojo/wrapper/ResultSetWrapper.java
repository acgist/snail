package com.acgist.snail.pojo.wrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库结果集包装器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ResultSetWrapper {

	/**
	 * 查询结果数据
	 */
	private final Map<String, Object> data = new HashMap<>();
	
	/**
	 * 设置查询结果
	 */
	public void put(String key, Object value) {
		this.data.put(key.toUpperCase(), value);
	}
	
	/**
	 * 获取字符串
	 */
	public String getString(String key) {
		return (String) getObject(key);
	}
	
	/**
	 * 获取integer
	 */
	public Integer getInteger(String key) {
		return (Integer) getObject(key);
	}

	/**
	 * 获取long
	 */
	public Long getLong(String key) {
		return (Long) getObject(key);
	}

	/**
	 * 获取日期
	 */
	public Date getDate(String key) {
		return (Date) getObject(key);
	}

	/**
	 * 获取对象
	 */
	public Object getObject(String key) {
		return this.data.get(key.toUpperCase());
	}

	@Override
	public String toString() {
		return this.data.toString();
	}
	
}
