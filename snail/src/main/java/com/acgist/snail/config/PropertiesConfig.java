package com.acgist.snail.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import com.acgist.snail.context.entity.ConfigEntity;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 配置文件
 * 
 * @author acgist
 */
public abstract class PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfig.class);
	
	/**
	 * 配置信息
	 */
	protected final Properties properties;

	/**
	 * 加载配置文件
	 * 
	 * @param path 配置文件相对路径
	 */
	protected PropertiesConfig(String path) {
		LOGGER.debug("加载配置文件：{}", path);
		this.properties = load(path);
	}
	
	/**
	 * 加载配置文件
	 * 
	 * @param path 配置文件相对路径
	 * 
	 * @return 配置信息
	 * 
	 * @see #loadFromUserDir(String)
	 * @see #loadFromResource(String)
	 */
	private static final Properties load(String path) {
		Properties properties = loadFromUserDir(path);
		if(properties == null) {
			properties = loadFromResource(path);
		}
		if(properties == null) {
			LOGGER.warn("加载配置文件失败：{}", path);
		}
		return properties;
	}
	
	/**
	 * 加载用户工作目录配置（UserDir）
	 * 
	 * @param path 配置文件相对路径
	 * 
	 * @return 配置信息
	 */
	private static final Properties loadFromUserDir(String path) {
		final File file = FileUtils.userDirFile(path);
		if(file == null || !file.exists()) {
			return null;
		}
		Properties properties = null;
		try(final var input = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			properties = new Properties();
			properties.load(input);
		} catch (IOException e) {
			LOGGER.error("读取配置文件异常：{}", path, e);
		}
		return properties;
	}
	
	/**
	 * 加载默认配置（Resource）
	 * 
	 * @param path 配置文件相对路径
	 * 
	 * @return 配置信息
	 */
	private static final Properties loadFromResource(String path) {
		if(PropertiesConfig.class.getResource(path) == null) {
			return null;
		}
		Properties properties = null;
		try(final var input = new InputStreamReader(PropertiesConfig.class.getResourceAsStream(path), StandardCharsets.UTF_8)) {
			properties = new Properties();
			properties.load(input);
		} catch (IOException e) {
			LOGGER.error("读取配置文件异常：{}", path, e);
		}
		return properties;
	}

	/**
	 * @return 是否加载成功
	 */
	protected final boolean hasProperties() {
		return this.properties != null;
	}

	/**
	 * 保存配置文件
	 * 
	 * @param data 数据
	 * @param path 路径
	 */
	protected final void persistent(Map<String, String> data, String path) {
		if(data == null || path == null) {
			LOGGER.warn("保存配置文件失败：{}-{}", data, path);
			return;
		}
		final File file = FileUtils.userDirFile(path);
		FileUtils.buildParentFolder(file);
		try(final var output = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			final Properties persistentProperties = new Properties();
			persistentProperties.putAll(data);
			persistentProperties.store(output, SystemConfig.getName());
		} catch (IOException e) {
			LOGGER.error("保存配置文件异常：{}", file, e);
		}
	}
	
	/**
	 * @param name 配置名称
	 * 
	 * @return 配置值
	 */
	protected final String getString(String name) {
		final String value = this.properties.getProperty(name);
		if(value == null) {
			LOGGER.debug("配置没有默认设置：{}", name);
		}
		return value;
	}
	
	/**
	 * @param name 配置名称
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected final String getString(String name, String defaultValue) {
		final String value = this.getString(name);
		return value == null ? defaultValue : value;
	}
	
	/**
	 * @param entity 实体配置
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected final String getString(ConfigEntity entity, String defaultValue) {
		return entity == null ? defaultValue : entity.getValue();
	}

	/**
	 * @param name 配置名称
	 * 
	 * @return 配置值
	 */
	protected final Boolean getBoolean(String name) {
		final String value = this.getString(name);
		if(Boolean.TRUE.toString().equalsIgnoreCase(value)) {
			return Boolean.TRUE;
		} else if(Boolean.FALSE.toString().equalsIgnoreCase(value)) {
			return Boolean.FALSE;
		} else {
			return null;
		}
	}
	
	/**
	 * @param name 配置名称
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected final boolean getBoolean(String name, boolean defaultValue) {
		final Boolean value = this.getBoolean(name);
		return value == null ? defaultValue : value;
	}

	/**
	 * @param entity 实体配置
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected final boolean getBoolean(ConfigEntity entity, boolean defaultValue) {
		return entity == null ? defaultValue : Boolean.parseBoolean(entity.getValue());
	}

	/**
	 * @param name 配置名称
	 * 
	 * @return 配置值
	 */
	protected final Integer getInteger(String name) {
		final String value = this.getString(name);
		if(StringUtils.isNumeric(value)) {
			return Integer.valueOf(value);
		}
		return null;
	}
	
	/**
	 * @param name 配置名称
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected final int getInteger(String name, int defaultValue) {
		final Integer value = this.getInteger(name);
		return value == null ? defaultValue : value;
	}

	/**
	 * @param entity 实体配置
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected final int getInteger(ConfigEntity entity, int defaultValue) {
		return entity == null ? defaultValue : Integer.parseInt(entity.getValue());
	}
	
	/**
	 * 释放配置
	 */
	protected final void release() {
		this.properties.clear();
	}
	
}
