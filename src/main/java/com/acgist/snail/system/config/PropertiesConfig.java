package com.acgist.snail.system.config;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.utils.PropertiesUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 配置超类
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class PropertiesConfig {

	protected PropertiesUtils properties;

	/**
	 * 初始化配置文件
	 * 
	 * @param file 配置文件
	 */
	public PropertiesConfig(String file) {
		this.properties = PropertiesUtils.getInstance(file);
	}
	
	/**
	 * 初始化配置文件
	 * 
	 * @param file 优先配置文件
	 * @param backFile 候选配置文件，如果优先配置文件加载失败使用。
	 */
	public PropertiesConfig(String file, String backFile) {
		this.properties = PropertiesUtils.getInstance(file);
		if(!this.properties.haveProperties()) {
			this.properties = PropertiesUtils.getInstance(backFile);
		}
	}
	
	protected String getString(String key) {
		return properties.getString(key);
	}

	protected Boolean getBoolean(String key) {
		return properties.getBoolean(key);
	}
	
	protected Integer getInteger(String key) {
		return properties.getInteger(key);
	}
	
	/**
	 * 获取String配置
	 */
	protected String configString(ConfigEntity entity, String defaultValue) {
		if(entity != null) {
			return entity.getValue();
		}
		return defaultValue;
	}
	
	/**
	 * 获取Boolean配置
	 */
	protected Boolean configBoolean(ConfigEntity entity, Boolean defaultValue) {
		if(entity != null) {
			final String value = entity.getValue();
			if(StringUtils.isNotEmpty(value)) {
				return Boolean.valueOf(value);
			}
		}
		return defaultValue;
	}
	
	/**
	 * 获取Integer配置
	 */
	protected Integer configInteger(ConfigEntity entity, Integer defaultValue) {
		if(entity != null) {
			final String value = entity.getValue();
			if(StringUtils.isNumeric(value)) {
				return Integer.valueOf(value);
			}
		}
		return defaultValue;
	}
	
}
