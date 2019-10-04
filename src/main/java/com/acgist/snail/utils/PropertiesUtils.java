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
public class PropertiesUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);
	
	private final Properties properties;

	private PropertiesUtils(Properties properties) {
		this.properties = properties;
	}
	
	/**
	 * <p>获取实例</p>
	 * <p>优先从UserDir加载，加载失败时加载默认配置。</p>
	 * 
	 * @param path 配置文件
	 */
	public static final PropertiesUtils getInstance(String path) {
		Properties properties = loadUserDir(path);
		if(properties == null) {
			properties = load(path);
		}
		return new PropertiesUtils(properties);
	}
	
	/**
	 * 加载数据（UserDir）
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
			LOGGER.error("读取配置文件异常，文件路径：{}", path, e);
		}
		return properties;
	}
	
	/**
	 * 加载数据（Resource）
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
			LOGGER.error("读取配置文件异常，文件路径：{}", path, e);
		}
		return properties;
	}
	
	/**
	 * 读取Boolean
	 * 
	 * @param name 属性名称
	 * 
	 * @return 属性值
	 */
	public Boolean getBoolean(String name) {
		return Boolean.valueOf(getString(name));
	}
	
	/**
	 * 读取Integer
	 * 
	 * @param name 属性名称
	 * 
	 * @return 属性值
	 */
	public Integer getInteger(String name) {
		return Integer.valueOf(getString(name));
	}
	
	/**
	 * 读取String
	 * 
	 * @param name 属性名称
	 * 
	 * @return 属性值
	 */
	public String getString(String name) {
		return properties.getProperty(name);
	}
	
	/**
	 * 判断是否有数据
	 */
	public boolean hasProperties() {
		return this.properties != null;
	}
	
	/**
	 * 获取数据
	 */
	public Properties properties() {
		return this.properties;
	}
	
}
