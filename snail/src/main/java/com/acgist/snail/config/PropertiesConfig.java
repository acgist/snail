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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>配置文件超类</p>
 * <p>读取配置文件并提供读取方法</p>
 * 
 * @author acgist
 */
public abstract class PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfig.class);
	
	/**
	 * <p>配置信息</p>
	 * <p>配置读取完成可以使用{@link #release()}释放资源</p>
	 */
	protected final Properties properties;

	/**
	 * <p>加载配置文件</p>
	 * 
	 * @param path 配置文件相对路径
	 */
	protected PropertiesConfig(String path) {
		LOGGER.debug("加载配置文件：{}", path);
		this.properties = load(path);
	}
	
	/**
	 * <p>加载配置文件</p>
	 * <p>优先加载用户工作目录配置（UserDir），如果加载失败则加载默认配置（Resource）。</p>
	 * 
	 * @param path 配置文件相对路径
	 * 
	 * @return 配置信息
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
	 * <p>加载用户工作目录配置（UserDir）</p>
	 * 
	 * @param path 配置文件相对路径
	 * 
	 * @return 配置信息
	 */
	private static final Properties loadFromUserDir(String path) {
		final File file = FileUtils.userDirFile(path);
		if(file == null || !file.exists()) {
			// 文件不存在
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
	 * <p>加载默认配置（Resource）</p>
	 * 
	 * @param path 配置文件相对路径
	 * 
	 * @return 配置信息
	 */
	private static final Properties loadFromResource(String path) {
		if(PropertiesConfig.class.getResource(path) == null) {
			// 资源不存在
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
	 * <p>判断配置是否加载成功</p>
	 * 
	 * @return true-成功；false-失败；
	 */
	protected final boolean hasProperties() {
		return this.properties != null;
	}

	/**
	 * <p>保存配置文件</p>
	 * 
	 * @param data 数据
	 * @param file 文件
	 */
	protected final void persistent(Map<String, String> data, File file) {
		if(data == null || file == null) {
			LOGGER.warn("保存配置文件失败：{}-{}", data, file);
			return;
		}
		FileUtils.buildFolder(file, true); // 创建上级目录
		try(final var output = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			// 不能使用内部变量：数据可能被清空
			final Properties persistentProperties = new Properties();
			persistentProperties.putAll(data);
			persistentProperties.store(output, SystemConfig.getName());
		} catch (IOException e) {
			LOGGER.error("保存配置文件异常：{}", file.getAbsolutePath(), e);
		}
	}
	
	/**
	 * <p>读取{@link String}配置</p>
	 * <p>没有配置默认返回：{@code null}</p>
	 * 
	 * @param name 配置名称
	 * 
	 * @return 配置值
	 */
	protected final String getString(String name) {
		return this.properties.getProperty(name);
	}
	
	/**
	 * <p>读取{@link String}配置</p>
	 * 
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
	 * <p>读取{@link String}配置</p>
	 * 
	 * @param entity 数据库配置
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected final String getString(ConfigEntity entity, String defaultValue) {
		return entity == null ? defaultValue : entity.getValue();
	}

	/**
	 * <p>读取{@link Boolean}配置</p>
	 * <p>没有配置默认返回：{@code null}</p>
	 * 
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
	 * <p>读取{@link Boolean}配置</p>
	 * 
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
	 * <p>读取{@link Boolean}配置</p>
	 * 
	 * @param entity 数据库配置
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected final boolean getBoolean(ConfigEntity entity, boolean defaultValue) {
		return entity == null ? defaultValue : Boolean.parseBoolean(entity.getValue());
	}

	/**
	 * <p>读取{@link Integer}配置</p>
	 * <p>没有配置默认返回：{@code null}</p>
	 * 
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
	 * <p>读取{@link Integer}配置</p>
	 * 
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
	 * <p>读取{@link Integer}配置</p>
	 * 
	 * @param entity 数据库配置
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected final int getInteger(ConfigEntity entity, int defaultValue) {
		return entity == null ? defaultValue : Integer.parseInt(entity.getValue());
	}
	
	/**
	 * <p>释放配置</p>
	 */
	protected final void release() {
		this.properties.clear();
	}
	
}
