package com.acgist.snail.module.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.impl.ConfigRepository;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.PropertiesUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 默认从配置文件加载，如果数据有配置则使用数据库配置替换
 */
public class DownloadConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadConfig.class);
	
	public static final String DOWNLOAD_PATH = "acgist.download.path";
	public static final String DOWNLOAD_SIZE = "acgist.download.size";
	public static final String DOWNLOAD_BUFFER = "acgist.download.buffer";
	public static final String DOWNLOAD_NOTICE = "acgist.download.notice";
	public static final String DOWNLOAD_P2P = "acgist.download.p2p";
	
	private static final DownloadConfig INSTANCE = new DownloadConfig();
	
	private DownloadConfig() {
	}
	
	private String downloadPath;
	private Integer downloadSize;
	private Integer downloadBuffer;
	private Boolean downloadNotice;
	private Boolean downloadP2p;
	
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
		ConfigRepository configRepository = new ConfigRepository();
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
	
	public static final void setDownloadPath(String path) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadPath = path;
		configRepository.updateConfig(DOWNLOAD_PATH, path);
	}
	
	public static final String getDownloadPath() {
		return FileUtils.folderPath(INSTANCE.downloadPath);
	}
	
	public static final String getDownloadPath(String fileName) throws DownloadException {
		if(StringUtils.isEmpty(fileName)) {
			throw new DownloadException("无效的下载路径：" + fileName);
		}
		return getDownloadPath() + "/" + fileName;
	}
	
	public static final void setDownloadSize(Integer downloadSize) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadSize = downloadSize;
		configRepository.updateConfig(DOWNLOAD_SIZE, String.valueOf(downloadSize));
	}

	public static final Integer getDownloadSize() {
		return INSTANCE.downloadSize;
	}
	
	public static final void setDownloadBuffer(Integer downloadBuffer) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadBuffer = downloadBuffer;
		configRepository.updateConfig(DOWNLOAD_BUFFER, String.valueOf(downloadBuffer));
	}
	
	public static final Integer getDownloadBuffer() {
		return INSTANCE.downloadBuffer;
	}

	public static final void setDownloadNotice(Boolean downloadNotice) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadNotice = downloadNotice;
		configRepository.updateConfig(DOWNLOAD_NOTICE, String.valueOf(downloadNotice));
	}
	
	public static final Boolean getDownloadNotice() {
		return INSTANCE.downloadNotice;
	}
	
	public static final void setDownloadP2p(Boolean downloadP2p) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadP2p = downloadP2p;
		configRepository.updateConfig(DOWNLOAD_P2P, String.valueOf(downloadP2p));
	}
	
	public static final Boolean getDownloadP2p() {
		return INSTANCE.downloadP2p;
	}
	
}
