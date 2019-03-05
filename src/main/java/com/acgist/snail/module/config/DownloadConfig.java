package com.acgist.snail.module.config;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.impl.ConfigRepository;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.PropertiesUtils;
import com.acgist.snail.utils.StringUtils;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * 下载配置（用户配置）：默认从配置文件加载，如果数据有配置则使用数据库配置替换
 */
public class DownloadConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadConfig.class);
	
	public static final String DOWNLOAD_PATH = "acgist.download.path";
	public static final String DOWNLOAD_SIZE = "acgist.download.size";
	public static final String DOWNLOAD_BUFFER = "acgist.download.buffer";
	public static final String DOWNLOAD_NOTICE = "acgist.download.notice";
	public static final String DOWNLOAD_P2P = "acgist.download.p2p";
	public static final String DOWNLOAD_LAST_PATH = "acgist.download.last.path";
	
	private static final DownloadConfig INSTANCE = new DownloadConfig();
	
	private DownloadConfig() {
	}
	
	static {
		LOGGER.info("初始化用户配置");
		INSTANCE.initFromProperties();
		INSTANCE.initFromDB();
		LOGGER.info("下载路径：{}", DOWNLOAD_PATH, INSTANCE.downloadPath);
		LOGGER.info("同时下载任务数量：{}", DOWNLOAD_SIZE, INSTANCE.downloadSize);
		LOGGER.info("单个任务下载速度（KB）：{}", DOWNLOAD_BUFFER, INSTANCE.downloadBuffer);
		LOGGER.info("下载完成弹出提示：{}", DOWNLOAD_NOTICE, INSTANCE.downloadNotice);
		LOGGER.info("启用P2P加速：{}", DOWNLOAD_P2P, INSTANCE.downloadP2p);
		LOGGER.info("最后一次选择文件目录：{}", DOWNLOAD_LAST_PATH, INSTANCE.downloadLastPath);
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
		INSTANCE.downloadLastPath = propertiesUtils.getString(DOWNLOAD_LAST_PATH);
	}
	
	private String downloadPath; // 下载路径
	private Integer downloadSize; // 同时下载任务数量
	private Integer downloadBuffer; // 单个任务下载速度（KB）
	private Boolean downloadNotice; // 下载完成弹出提示
	private Boolean downloadP2p; // 启用P2P加速
	private String downloadLastPath; // 最后一次选择文件目录
	
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
		entity = configRepository.findOne(ConfigEntity.PROPERTY_NAME, DOWNLOAD_LAST_PATH);
		downloadLastPath = configString(entity, downloadLastPath);
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
		return FileUtils.file(getDownloadPath(), fileName);
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
		if(downloadBuffer == 0) {
			LOGGER.warn("下载速度不能：0，设置为最小值：1");
			downloadBuffer = 1; // 下载速度不能设置：0
		}
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
	
	public static final void setDownloadLastPath(String downloadLastPath) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadLastPath = downloadLastPath;
		configRepository.updateConfig(DOWNLOAD_P2P, downloadLastPath);
	}
	
	public static final String getDownloadLastPath() {
		return INSTANCE.downloadLastPath;
	}
	
	private static final File lastPath() {
		File file = null;
		if(StringUtils.isEmpty(INSTANCE.downloadLastPath)) {
			file = new File(getDownloadPath());
		} else {
			file = new File(INSTANCE.downloadLastPath);
		}
		return file;
	}
	
	public static final void lastPath(FileChooser chooser) {
		File file = lastPath();
		if(file != null && file.exists()) {
			chooser.setInitialDirectory(file);
		}
	}
	
	public static final void lastPath(DirectoryChooser chooser) {
		File file = lastPath();
		if(file != null && file.exists()) {
			chooser.setInitialDirectory(file);
		}
	}
	
}
