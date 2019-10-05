package com.acgist.snail.system.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.PropertiesUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>配置</p>
 * <p>优先加载用户配置（UserDir目录），加载失败时加载默认配置。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfig.class);

	/**
	 * TODO：init后是否清除
	 */
	protected PropertiesUtils properties;

	/**
	 * 初始化配置文件
	 * 
	 * @param path 配置文件路径
	 */
	public PropertiesConfig(String path) {
		this.properties = PropertiesUtils.getInstance(path);
	}
	
	protected String getString(String key) {
		return this.properties.getString(key);
	}

	protected Boolean getBoolean(String key) {
		return this.properties.getBoolean(key);
	}
	
	protected Integer getInteger(String key) {
		return this.properties.getInteger(key);
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
	
	/**
	 * 保存配置
	 * 
	 * @param data 数据
	 * @param file 文件
	 */
	protected void persistent(Map<String, String> data, File file) {
		if(data == null || file == null) {
			LOGGER.warn("保存配置失败：{}-{}", data, file);
			return;
		}
		FileUtils.buildFolder(file, true);
		try(final var output = new OutputStreamWriter(new FileOutputStream(file), SystemConfig.DEFAULT_CHARSET)) {
			final Properties properties = new Properties();
			properties.putAll(data);
			properties.store(output, SystemConfig.getName());
		} catch (IOException e) {
			LOGGER.error("保存配置异常，文件路径：{}", file.getPath(), e);
		}
	}
	
}
