package com.acgist.snail.service;

import java.io.File;

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
	public static final String DOWNLOAD_NOTICE = "acgist.download.notice";
	public static final String DOWNLOAD_P2P = "acgist.download.p2p";
	
	private static final ConfigService INSTANCE = new ConfigService();
	
	private ConfigService() {
	}
	
	public static final ConfigService getInstance() {
		return INSTANCE;
	}
	
	private String downloadPath;
	private Integer downloadSize;
	private Integer downloadBuffer;
	private Boolean downloadNotice;
	private Boolean downloadP2p;
	
	private ConfigRepository configRepository = new ConfigRepository();
	
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
		PropertiesUtils propertiesUtils = PropertiesUtils.getInstance("/config/config.download.properties");
		INSTANCE.downloadPath = propertiesUtils.getString(DOWNLOAD_PATH);
		INSTANCE.downloadSize = propertiesUtils.getInteger(DOWNLOAD_SIZE);
		INSTANCE.downloadBuffer = propertiesUtils.getInteger(DOWNLOAD_BUFFER);
		INSTANCE.downloadNotice = propertiesUtils.getBoolean(DOWNLOAD_NOTICE);
		INSTANCE.downloadP2p = propertiesUtils.getBoolean(DOWNLOAD_P2P);
	}
	
	/**
	 * 数据库初始化配置
	 */
	private void initFromDB() {
		ConfigEntity entity = null;
		entity = configRepository.findOne(ConfigEntity.PROPERTY_NAME, DOWNLOAD_PATH);
		downloadPath = configString(entity, downloadPath);
		entity = configRepository.findOne(ConfigEntity.PROPERTY_NAME, DOWNLOAD_SIZE);
		downloadSize = configInteger(entity, downloadSize);
		entity = configRepository.findOne(ConfigEntity.PROPERTY_NAME, DOWNLOAD_BUFFER);
		downloadBuffer = configInteger(entity, downloadBuffer);
		entity = configRepository.findOne(ConfigEntity.PROPERTY_NAME, DOWNLOAD_NOTICE);
		downloadNotice = configBoolean(entity, downloadNotice);
		entity = configRepository.findOne(ConfigEntity.PROPERTY_NAME, DOWNLOAD_P2P);
		downloadP2p = configBoolean(entity, downloadP2p);
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
	 * 获取Integer配置
	 */
	private Boolean configBoolean(ConfigEntity entity, Boolean defaultValue) {
		if(entity != null) {
			String value = entity.getValue();
			if(StringUtils.isNotEmpty(value)) {
				return Boolean.valueOf(value);
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
		LOGGER.info("下载完成弹出提示，属性：{}，值：{}", DOWNLOAD_NOTICE, downloadNotice);
		LOGGER.info("启用P2P加速，属性：{}，值：{}", DOWNLOAD_P2P, downloadP2p);
	}
	
	public void setDownloadPath(String path) {
		this.downloadPath = path;
		configRepository.updateConfig(DOWNLOAD_PATH, path);
	}
	
	public String getDownloadPath() {
		File file = new File(this.downloadPath);
		if(file.exists()) {
			return this.downloadPath;
		}
		String path = System.getProperty("user.dir") + this.downloadPath;
		file = new File(path);
		if(file.exists()) {
			return path;
		}
		file.mkdirs();
		return path;
	}
	
	public void setDownloadSize(Integer downloadSize) {
		this.downloadSize = downloadSize;
		configRepository.updateConfig(DOWNLOAD_SIZE, String.valueOf(downloadSize));
	}

	public Integer getDownloadSize() {
		return downloadSize;
	}
	
	public void setDownloadBuffer(Integer downloadBuffer) {
		this.downloadBuffer = downloadBuffer;
		configRepository.updateConfig(DOWNLOAD_BUFFER, String.valueOf(downloadBuffer));
	}
	
	public Integer getDownloadBuffer() {
		return downloadBuffer;
	}

	public void setDownloadNotice(Boolean downloadNotice) {
		this.downloadNotice = downloadNotice;
		configRepository.updateConfig(DOWNLOAD_NOTICE, String.valueOf(downloadNotice));
	}
	
	public Boolean getDownloadNotice() {
		return downloadNotice;
	}
	
	public void setDownloadP2p(Boolean downloadP2p) {
		this.downloadP2p = downloadP2p;
		configRepository.updateConfig(DOWNLOAD_P2P, String.valueOf(downloadP2p));
	}
	
	public Boolean getDownloadP2p() {
		return downloadP2p;
	}
	
}
