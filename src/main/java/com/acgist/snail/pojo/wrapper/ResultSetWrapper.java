package com.acgist.snail.pojo.wrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * wrapper - 数据库结果集
 */
public class ResultSetWrapper {

	private Map<String, Object> data = new HashMap<>();
	
	public void put(String key, Object value) {
		data.put(key.toUpperCase(), value);
	}
	
	public String getString(String key) {
		return (String) getObject(key);
	}
	
	public Integer getInteger(String key) {
		return (Integer) getObject(key);
	}
	
	public Date getDate(String key) {
		return (Date) getObject(key);
	}
	
	public Object getObject(String key) {
		return data.get(key.toUpperCase());
	}

	public String toString() {
		return this.data.toString();
	}
	
}
