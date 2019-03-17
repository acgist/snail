package com.acgist.snail.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;

/**
 * utils - 配置
 */
public class PropertiesUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);
	
	private Properties properties;

	private PropertiesUtils() {
	}
	
	/**
	 * 获取实例
	 * @param file 配置文件
	 */
	public static final PropertiesUtils getInstance(String file) {
		PropertiesUtils instance = new PropertiesUtils();
		instance.properties = load(file);
		return instance;
	}
	
	private static final Properties load(String file) {
		Properties properties = new Properties();
		try(InputStreamReader input = new InputStreamReader(PropertiesUtils.class.getResourceAsStream(file), SystemConfig.DEFAULT_CHARSET)) {
			properties.load(input);
		} catch (IOException e) {
			LOGGER.error("读取配置文件异常，文件路径：{}", file, e);
		}
		return properties;
	}
	
	public Boolean getBoolean(String name) {
		return Boolean.valueOf(getString(name));
	}
	
	public Integer getInteger(String name) {
		return Integer.valueOf(getString(name));
	}
	
	public String getString(String name) {
		return properties.getProperty(name);
	}
	
}
