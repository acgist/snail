package com.acgist.snail.service;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.ConfigEntity;

/**
 * 配置，默认值从文件（application-system.properties）加载，配置修改后保存数据库，以后从数据库加载配置。<br>
 * 属性详细说明参考：application-config.properties
 */
public class ConfigService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);
	
	public static final String DOWNLOAD_PATH = "acgist.download.path";
	public static final String DOWNLOAD_SIZE = "acgist.download.size";
	public static final String DOWNLOAD_BUFFER = "acgist.download.buffer";
	
	private String downloadPath;
	private Integer downloadSize;
	private Integer downloadBuffer;
	
//	@Autowired
//	private Environment environment;
//	@Autowired
//	private ConfigRepository configRepository;
	
	public void init() {
		LOGGER.info("配置初始化");
		initFromConfig();
		initFromDB();
		loggerInfo();
	}
	
	/**
	 * 配置文件加载
	 */
	private void initFromConfig() {
//		downloadPath = environment.getProperty(DOWNLOAD_PATH, "./download");
//		downloadSize = environment.getProperty(DOWNLOAD_SIZE, Integer.class, 4);
//		downloadBuffer = environment.getProperty(DOWNLOAD_BUFFER, Integer.class, 1024);
	}
	
	/**
	 * 数据库初始化配置
	 */
	private void initFromDB() {
//		Optional<ConfigEntity> optional = null;
//		optional = configRepository.findProperty(ConfigEntity.PROPERTY_NAME, DOWNLOAD_PATH);
//		downloadPath = configString(optional, downloadPath);
//		optional = configRepository.findProperty(ConfigEntity.PROPERTY_NAME, DOWNLOAD_SIZE);
//		downloadSize = configInteger(optional, downloadSize);
//		optional = configRepository.findProperty(ConfigEntity.PROPERTY_NAME, DOWNLOAD_BUFFER);
//		downloadBuffer = configInteger(optional, downloadBuffer);
	}
	
	/**
	 * 获取String配置
	 */
	private String configString(Optional<ConfigEntity> optional, String defaultValue) {
		if(optional.isPresent()) {
			return optional.get().getValue();
		}
		return defaultValue;
	}
	
	/**
	 * 获取Integer配置
	 */
	private Integer configInteger(Optional<ConfigEntity> optional, Integer defaultValue) {
		if(optional.isPresent()) {
			String value = optional.get().getValue();
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
