package com.acgist.snail.system.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>配置文件</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfig.class);
	
	/**
	 * <p>配置信息</p>
	 */
	protected final Properties properties;

	/**
	 * <p>加载配置文件</p>
	 * 
	 * @param path 配置文件路径
	 */
	protected PropertiesConfig(String path) {
		this.properties = load(path);
	}
	
	/**
	 * <p>加载配置文件</p>
	 * <p>优先加载用户工作目录（UserDir）配置，如果加载失败则加载默认（Resource）配置。</p>
	 * 
	 * @param path 配置文件相对路径：{@code /}开头
	 * 
	 * @return 配置信息
	 */
	private static final Properties load(String path) {
		Properties properties = loadFromUserDir(path);
		if(properties == null) {
			properties = loadFromResource(path);
		}
		return properties;
	}
	
	/**
	 * <p>加载用户工作目录配置（UserDir）</p>
	 * 
	 * @param path 文件路径
	 * 
	 * @return 配置信息
	 */
	private static final Properties loadFromUserDir(String path) {
		final File file = FileUtils.userDirFile(path);
		if(file == null || !file.exists()) {
			return null;
		}
		Properties properties = null;
		try(final var input = new InputStreamReader(new FileInputStream(file), SystemConfig.DEFAULT_CHARSET)) {
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
	 * @param path 文件路径
	 * 
	 * @return 配置信息
	 */
	private static final Properties loadFromResource(String path) {
		if(PropertiesConfig.class.getResource(path) == null) {
			return null;
		}
		Properties properties = null;
		try(final var input = new InputStreamReader(PropertiesConfig.class.getResourceAsStream(path), SystemConfig.DEFAULT_CHARSET)) {
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
	 * @return {@code true}-成功；{@code false}-失败；
	 */
	public boolean haveProperties() {
		return this.properties != null;
	}

	/**
	 * <p>保存配置</p>
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
			LOGGER.error("保存配置异常：{}", file.getPath(), e);
		}
	}
	
	/**
	 * <p>读取{@code String}配置</p>
	 * <p>没有配置默认返回：{@code null}</p>
	 * 
	 * @param name 配置名称
	 * 
	 * @return 配置值
	 */
	protected String getString(String name) {
		return this.properties.getProperty(name);
	}
	
	/**
	 * <p>读取{@code String}配置</p>
	 * 
	 * @param name 配置名称
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected String getString(String name, String defaultValue) {
		final String value = this.getString(name);
		return value == null ? defaultValue : value;
	}
	
	/**
	 * <p>读取{@code String}配置</p>
	 * 
	 * @param entity 数据库配置
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected String getString(ConfigEntity entity, String defaultValue) {
		return entity == null ? defaultValue : entity.getValue();
	}

	/**
	 * <p>读取{@code Boolean}配置</p>
	 * <p>没有配置默认返回：{@code null}</p>
	 * 
	 * @param name 配置名称
	 * 
	 * @return 配置值
	 */
	protected Boolean getBoolean(String name) {
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
	 * <p>读取{@code Boolean}配置</p>
	 * 
	 * @param name 配置名称
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected boolean getBoolean(String name, boolean defaultValue) {
		final Boolean value = this.getBoolean(name);
		return value == null ? defaultValue : value;
	}

	/**
	 * <p>读取{@code Boolean}配置</p>
	 * 
	 * @param entity 数据库配置
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected boolean getBoolean(ConfigEntity entity, boolean defaultValue) {
		return entity == null ? defaultValue : Boolean.parseBoolean(entity.getValue());
	}

	/**
	 * <p>读取{@code Integer}配置</p>
	 * <p>没有配置默认返回：{@code null}</p>
	 * 
	 * @param name 配置名称
	 * 
	 * @return 配置值
	 */
	protected Integer getInteger(String name) {
		final String value = this.getString(name);
		if(StringUtils.isNumeric(value)) {
			return Integer.valueOf(value);
		}
		return null;
	}
	
	/**
	 * <p>读取{@code Integer}配置</p>
	 * 
	 * @param naame 配置名称
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected int getInteger(String naame, int defaultValue) {
		final Integer value = this.getInteger(naame);
		return value == null ? defaultValue : value;
	}

	/**
	 * <p>读取{@code Integer}配置</p>
	 * 
	 * @param entity 数据库配置
	 * @param defaultValue 默认值
	 * 
	 * @return 配置值
	 */
	protected int getInteger(ConfigEntity entity, int defaultValue) {
		return entity == null ? defaultValue : Integer.parseInt(entity.getValue());
	}
	
}
