package com.acgist.snail.pojo.wrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>结果集包装器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ResultSetWrapper {

	/**
	 * <p>结果集数据</p>
	 * <p>字段名称（大写）=字段值</p>
	 */
	private final Map<String, Object> data = new HashMap<>();
	
	/**
	 * <p>设置结果集数据</p>
	 * 
	 * @param key 字段名称
	 * @param value 字段值
	 */
	public void put(String key, Object value) {
		this.data.put(key.toUpperCase(), value);
	}
	
	/**
	 * <p>获取字符串</p>
	 * 
	 * @param key 字段名称
	 * 
	 * @return 字符串
	 */
	public String getString(String key) {
		return (String) getObject(key);
	}
	
	/**
	 * <p>获取Integer</p>
	 * 
	 * @param key 字段名称
	 * 
	 * @return Integer
	 */
	public Integer getInteger(String key) {
		return (Integer) getObject(key);
	}

	/**
	 * <p>获取Long</p>
	 * 
	 * @param key 字段名称
	 * 
	 * @return Long
	 */
	public Long getLong(String key) {
		return (Long) getObject(key);
	}

	/**
	 * <p>获取时间</p>
	 * 
	 * @param key 字段名称
	 * 
	 * @return 时间
	 */
	public Date getDate(String key) {
		return (Date) getObject(key);
	}

	/**
	 * <p>获取对象</p>
	 * 
	 * @param key 字段名称
	 * 
	 * @return 对象
	 */
	public Object getObject(String key) {
		Objects.requireNonNull(key, "字段名称为空");
		return this.data.get(key.toUpperCase());
	}

	@Override
	public String toString() {
		return this.data.toString();
	}
	
}
