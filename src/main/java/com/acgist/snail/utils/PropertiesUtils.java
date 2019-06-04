package com.acgist.snail.utils;

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
	
	private Properties properties;

	private PropertiesUtils() {
	}
	
	/**
	 * 获取实例
	 * 
	 * @param file 配置文件
	 */
	public static final PropertiesUtils getInstance(String file) {
		final PropertiesUtils instance = new PropertiesUtils();
		instance.properties = load(file);
		return instance;
	}
	
	/**
	 * 加载数据，如果文件不存在返回null。
	 */
	private static final Properties load(String file) {
		if(PropertiesUtils.class.getResource(file) == null) {
			return null;
		}
		Properties properties = null;
		try(InputStreamReader input = new InputStreamReader(PropertiesUtils.class.getResourceAsStream(file), SystemConfig.DEFAULT_CHARSET)) {
			properties = new Properties();
			properties.load(input);
		} catch (IOException e) {
			LOGGER.error("读取配置文件异常，文件路径：{}", file, e);
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
	public boolean haveProperties() {
		return this.properties != null;
	}
	
	/**
	 * 获取数据
	 */
	public Properties properties() {
		return this.properties;
	}
	
}
