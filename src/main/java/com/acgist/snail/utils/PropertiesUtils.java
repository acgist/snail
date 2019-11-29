package com.acgist.snail.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;

/**
 * <p>Properties工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class PropertiesUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);
	
	private final Properties properties;

	private PropertiesUtils(Properties properties) {
		this.properties = properties;
	}
	
	/**
	 * <p>创建实例</p>
	 * <p>优先从UserDir加载配置，加载失败后加载默认配置。</p>
	 * 
	 * @param path 配置文件
	 * 
	 * @return Properties工具
	 */
	public static final PropertiesUtils getInstance(String path) {
		Properties properties = loadUserDir(path);
		if(properties == null) {
			properties = load(path);
		}
		return new PropertiesUtils(properties);
	}
	
	/**
	 * <p>加载配置（UserDir）</p>
	 */
	private static final Properties loadUserDir(String path) {
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
	 * <p>加载配置（Resource）</p>
	 */
	private static final Properties load(String path) {
		if(PropertiesUtils.class.getResource(path) == null) {
			return null;
		}
		Properties properties = null;
		try(final var input = new InputStreamReader(PropertiesUtils.class.getResourceAsStream(path), SystemConfig.DEFAULT_CHARSET)) {
			properties = new Properties();
			properties.load(input);
		} catch (IOException e) {
			LOGGER.error("读取配置文件异常：{}", path, e);
		}
		return properties;
	}
	
	/**
	 * <p>读取String</p>
	 * 
	 * @param name 属性名称
	 * 
	 * @return 属性值
	 */
	public String getString(String name) {
		return properties.getProperty(name);
	}
	
	/**
	 * <p>读取Boolean</p>
	 * <p>默认：null</p>
	 * 
	 * @param name 属性名称
	 * 
	 * @return 属性值
	 */
	public Boolean getBoolean(String name) {
		final String value = getString(name);
		if(Boolean.TRUE.toString().equalsIgnoreCase(value)) {
			return true;
		} else if(Boolean.FALSE.toString().equalsIgnoreCase(value)) {
			return false;
		} else {
			return null;
		}
	}
	
	/**
	 * <p>读取Integer</p>
	 * <p>默认：null</p>
	 * 
	 * @param name 属性名称
	 * 
	 * @return 属性值
	 */
	public Integer getInteger(String name) {
		final String value = getString(name);
		if(StringUtils.isNumeric(value)) {
			return Integer.parseInt(value);
		}
		return null;
	}
	
	/**
	 * @return 配置
	 */
	public Properties properties() {
		return this.properties;
	}
	
	/**
	 * <p>是否存在配置</p>
	 * 
	 * @return true-存在；false-不存在；
	 */
	public boolean haveProperties() {
		return this.properties != null;
	}
	
}
