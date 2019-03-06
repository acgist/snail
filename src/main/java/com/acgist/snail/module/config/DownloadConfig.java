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
	public static final String DOWNLOAD_MEMORY_BUFFER = "acgist.download.memory.buffer";
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
		LOGGER.info("下载目录：{}", INSTANCE.downloadPath);
		LOGGER.info("下载任务数量：{}", INSTANCE.downloadSize);
		LOGGER.info("下载速度（单个）（KB）：{}", INSTANCE.downloadBuffer);
		LOGGER.info("磁盘缓存（单个）（MB）：{}", INSTANCE.downloadMemoryBuffer);
		LOGGER.info("消息提示：{}", INSTANCE.downloadNotice);
		LOGGER.info("启用P2P加速：{}", INSTANCE.downloadP2p);
		LOGGER.info("最后一次选择目录：{}", INSTANCE.downloadLastPath);
	}
	
	/**
	 * 配置文件加载
	 */
	private void initFromProperties() {
		PropertiesUtils propertiesUtils = PropertiesUtils.getInstance("/config/config.download.properties");
		INSTANCE.downloadPath = propertiesUtils.getString(DOWNLOAD_PATH);
		INSTANCE.downloadSize = propertiesUtils.getInteger(DOWNLOAD_SIZE);
		INSTANCE.downloadBuffer = propertiesUtils.getInteger(DOWNLOAD_BUFFER);
		INSTANCE.downloadMemoryBuffer = propertiesUtils.getInteger(DOWNLOAD_MEMORY_BUFFER);
		INSTANCE.downloadNotice = propertiesUtils.getBoolean(DOWNLOAD_NOTICE);
		INSTANCE.downloadP2p = propertiesUtils.getBoolean(DOWNLOAD_P2P);
		INSTANCE.downloadLastPath = propertiesUtils.getString(DOWNLOAD_LAST_PATH);
	}
	
	private String downloadPath; // 下载目录
	private Integer downloadSize; // 下载任务数量
	private Integer downloadBuffer; // 下载速度（单个）（KB）
	private Integer downloadMemoryBuffer; // 磁盘缓存（单个）（MB）
	private Boolean downloadNotice; // 消息提示
	private Boolean downloadP2p; // 启用P2P加速
	private String downloadLastPath; // 最后一次选择目录
	
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
		entity = configRepository.findOne(ConfigEntity.PROPERTY_NAME, DOWNLOAD_MEMORY_BUFFER);
		downloadMemoryBuffer = configInteger(entity, downloadMemoryBuffer);
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
	
	/**
	 * 设置下载目录
	 */
	public static final void setDownloadPath(String path) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadPath = path;
		configRepository.updateConfig(DOWNLOAD_PATH, path);
	}
	
	/**
	 * 下载目录
	 */
	public static final String getDownloadPath() {
		return FileUtils.folderPath(INSTANCE.downloadPath);
	}

	/**
	 * 获取下载目录：下载目录+文件名称
	 */
	public static final String getDownloadPath(String fileName) throws DownloadException {
		if(StringUtils.isEmpty(fileName)) {
			throw new DownloadException("无效的下载路径：" + fileName);
		}
		return FileUtils.file(getDownloadPath(), fileName);
	}
	
	/**
	 * 设置下载任务数量
	 */
	public static final void setDownloadSize(Integer downloadSize) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadSize = downloadSize;
		configRepository.updateConfig(DOWNLOAD_SIZE, String.valueOf(downloadSize));
	}

	/**
	 * 下载任务数量
	 */
	public static final Integer getDownloadSize() {
		return INSTANCE.downloadSize;
	}
	
	/**
	 * 设置下载速度（单个）（KB）
	 */
	public static final void setDownloadBuffer(Integer downloadBuffer) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadBuffer = downloadBuffer;
		configRepository.updateConfig(DOWNLOAD_BUFFER, String.valueOf(downloadBuffer));
	}
	
	/**
	 * 下载速度（单个）（KB）
	 */
	public static final Integer getDownloadBuffer() {
		return INSTANCE.downloadBuffer;
	}

	/**
	 * 下载速度（单个）（B）
	 */
	public static final Integer getDownloadBufferByte() {
		return INSTANCE.downloadBuffer * 1024;
	}
	
	/**
	 * 磁盘缓存（单个）（MB）
	 */
	public static final void setDownloadMemoryBuffer(Integer downloadMemoryBuffer) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadMemoryBuffer = downloadMemoryBuffer;
		configRepository.updateConfig(DOWNLOAD_MEMORY_BUFFER, String.valueOf(downloadMemoryBuffer));
	}

	/**
	 * 磁盘缓存（单个）（MB）
	 */
	public static final Integer getDownloadMemoryBuffer() {
		return INSTANCE.downloadMemoryBuffer;
	}

	/**
	 * 磁盘缓存（单个）（B）
	 */
	public static final Integer getDownloadMemoryBufferByte() {
		return INSTANCE.downloadMemoryBuffer * 1024 * 1024;
	}

	/**
	 * 设置消息提示
	 */
	public static final void setDownloadNotice(Boolean downloadNotice) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadNotice = downloadNotice;
		configRepository.updateConfig(DOWNLOAD_NOTICE, String.valueOf(downloadNotice));
	}

	/**
	 * 消息提示
	 */
	public static final Boolean getDownloadNotice() {
		return INSTANCE.downloadNotice;
	}
	
	/**
	 * 设置启用P2P加速
	 */
	public static final void setDownloadP2p(Boolean downloadP2p) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadP2p = downloadP2p;
		configRepository.updateConfig(DOWNLOAD_P2P, String.valueOf(downloadP2p));
	}
	
	/**
	 * 启用P2P加速
	 */
	public static final Boolean getDownloadP2p() {
		return INSTANCE.downloadP2p;
	}
	
	/**
	 * 设置最后一次选择目录
	 */
	public static final void setDownloadLastPath(String downloadLastPath) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.downloadLastPath = downloadLastPath;
		configRepository.updateConfig(DOWNLOAD_P2P, downloadLastPath);
	}
	
	/**
	 * 最后一次选择目录
	 */
	public static final String getDownloadLastPath() {
		return INSTANCE.downloadLastPath;
	}

	/**
	 * 最后一次选择目录文件，如果没有选择默认使用下载目录
	 */
	private static final File lastPath() {
		File file = null;
		if(StringUtils.isEmpty(INSTANCE.downloadLastPath)) {
			file = new File(getDownloadPath());
		} else {
			file = new File(INSTANCE.downloadLastPath);
		}
		return file;
	}
	
	/**
	 * 最后一次选择目录
	 */
	public static final void lastPath(FileChooser chooser) {
		File file = lastPath();
		if(file != null && file.exists()) {
			chooser.setInitialDirectory(file);
		}
	}
	
	/**
	 * 最后一次选择目录
	 */
	public static final void lastPath(DirectoryChooser chooser) {
		File file = lastPath();
		if(file != null && file.exists()) {
			chooser.setInitialDirectory(file);
		}
	}
	
}
