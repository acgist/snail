package com.acgist.snail.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.impl.ConfigRepository;
import com.acgist.snail.utils.PropertiesUtils;

/**
 * 默认从配置文件加载，如果数据有配置则使用数据库配置替换
 */
public class ConfigService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);
	
	public static final String DOWNLOAD_PATH = "acgist.download.path";
	public static final String DOWNLOAD_SIZE = "acgist.download.size";
	public static final String DOWNLOAD_BUFFER = "acgist.download.buffer";
	
	private static final ConfigService INSTANCE = new ConfigService();
	
	private ConfigService() {
	}
	
	private String downloadPath;
	private Integer downloadSize;
	private Integer downloadBuffer;
	
	static {
		LOGGER.info("初始化用户配置");
		INSTANCE.initFromProperties();
		INSTANCE.initFromDB();
		INSTANCE.loggerInfo();
	}
	
	/**
	 * 配置文件加载
	 */
	private void initFromProperties() {
		PropertiesUtils propertiesUtils = PropertiesUtils.getInstance("/config/config.properties");
		INSTANCE.downloadPath = propertiesUtils.getString(DOWNLOAD_PATH);
		INSTANCE.downloadSize = propertiesUtils.getInteger(DOWNLOAD_SIZE);
		INSTANCE.downloadBuffer = propertiesUtils.getInteger(DOWNLOAD_BUFFER);
	}
	
	/**
	 * 数据库初始化配置
	 */
	private void initFromDB() {
		ConfigRepository configRepository = new ConfigRepository();
		ConfigEntity entity = null;
		entity = configRepository.findOne(ConfigEntity.PROPERTY_NAME, DOWNLOAD_PATH);
		downloadPath = configString(entity, downloadPath);
		entity = configRepository.findOne(ConfigEntity.PROPERTY_NAME, DOWNLOAD_SIZE);
		downloadSize = configInteger(entity, downloadSize);
		entity = configRepository.findOne(ConfigEntity.PROPERTY_NAME, DOWNLOAD_BUFFER);
		downloadBuffer = configInteger(entity, downloadBuffer);
	}
	
	/**
	 * 获取String配置
	 */
	private String configString(ConfigEntity entity, String defaultValue) {
		if(entity != null) {
			return entity.getValue();
		}
		return defaultValue;
	}
	
	/**
	 * 获取Integer配置
	 */
	private Integer configInteger(ConfigEntity entity, Integer defaultValue) {
		if(entity != null) {
			String value = entity.getValue();
			if(StringUtils.isNumeric(value)) {
				return Integer.valueOf(value);
			}
		}
		return defaultValue;
	}
	
	/**
	 * 日志记录
	 */
	private void loggerInfo() {
		LOGGER.info("下载路径，属性：{}，值：{}", DOWNLOAD_PATH, downloadPath);
		LOGGER.info("同时下载任务数量，属性：{}，值：{}", DOWNLOAD_SIZE, downloadSize);
		LOGGER.info("单个任务下载速度（KB），属性：{}，值：{}", DOWNLOAD_BUFFER, downloadBuffer);
	}
	
	/**
	 * 保存配置到数据库
	 */
	public void saveToDB() {
	}

	public String getDownloadPath() {
		return downloadPath;
	}

	public Integer getDownloadSize() {
		return downloadSize;
	}

	public Integer getDownloadBuffer() {
		return downloadBuffer;
	}
	
}
