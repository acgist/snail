package com.acgist.snail.system.config;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.impl.ConfigRepository;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * 下载配置（用户配置）：默认从配置文件加载，如果数据有配置则使用数据库配置替换
 */
public class DownloadConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadConfig.class);
	
	private static final String DOWNLOAD_CONFIG = "/config/download.properties";
	
	public static final String DOWNLOAD_PATH = "acgist.download.path";
	public static final String DOWNLOAD_SIZE = "acgist.download.size";
	public static final String DOWNLOAD_BUFFER = "acgist.download.buffer";
	public static final String DOWNLOAD_MEMORY_BUFFER = "acgist.download.memory.buffer";
	public static final String DOWNLOAD_NOTICE = "acgist.download.notice";
	public static final String DOWNLOAD_P2P = "acgist.download.p2p";
	public static final String DOWNLOAD_LAST_PATH = "acgist.download.last.path";
	
	private static final DownloadConfig INSTANCE = new DownloadConfig();
	
	private DownloadConfig() {
		super(DOWNLOAD_CONFIG);
	}
	
	static {
		LOGGER.info("初始化用户配置");
		INSTANCE.initFromProperties();
		INSTANCE.initFromDB();
		INSTANCE.logger();
	}
	
	public static final DownloadConfig getInstance() {
		return INSTANCE;
	}
	
	private String path; // 下载目录
	private Integer size; // 下载任务数量
	private Integer buffer; // 下载速度（单个）（KB）
	private Integer memoryBuffer; // 磁盘缓存（单个）（MB）
	private Boolean notice; // 消息提示
	private Boolean p2p; // 启用P2P加速
	private String lastPath; // 最后一次选择目录
	
	/**
	 * 配置文件加载
	 */
	private void initFromProperties() {
		INSTANCE.path = getString(DOWNLOAD_PATH);
		INSTANCE.size = getInteger(DOWNLOAD_SIZE);
		INSTANCE.buffer = getInteger(DOWNLOAD_BUFFER);
		INSTANCE.memoryBuffer = getInteger(DOWNLOAD_MEMORY_BUFFER);
		INSTANCE.notice = getBoolean(DOWNLOAD_NOTICE);
		INSTANCE.p2p = getBoolean(DOWNLOAD_P2P);
		INSTANCE.lastPath = getString(DOWNLOAD_LAST_PATH);
	}
	
	/**
	 * 数据库初始化配置
	 */
	private void initFromDB() {
		ConfigRepository configRepository = new ConfigRepository();
		ConfigEntity entity = null;
		entity = configRepository.findName(DOWNLOAD_PATH);
		path = configString(entity, path);
		entity = configRepository.findName(DOWNLOAD_SIZE);
		size = configInteger(entity, size);
		entity = configRepository.findName(DOWNLOAD_BUFFER);
		buffer = configInteger(entity, buffer);
		entity = configRepository.findName(DOWNLOAD_MEMORY_BUFFER);
		memoryBuffer = configInteger(entity, memoryBuffer);
		entity = configRepository.findName(DOWNLOAD_NOTICE);
		notice = configBoolean(entity, notice);
		entity = configRepository.findName(DOWNLOAD_P2P);
		p2p = configBoolean(entity, p2p);
		entity = configRepository.findName(DOWNLOAD_LAST_PATH);
		lastPath = configString(entity, lastPath);
	}
	
	/**
	 * 日志
	 */
	private void logger() {
		LOGGER.info("下载目录：{}", INSTANCE.path);
		LOGGER.info("下载任务数量：{}", INSTANCE.size);
		LOGGER.info("下载速度（单个）（KB）：{}", INSTANCE.buffer);
		LOGGER.info("磁盘缓存（单个）（MB）：{}", INSTANCE.memoryBuffer);
		LOGGER.info("消息提示：{}", INSTANCE.notice);
		LOGGER.info("启用P2P加速：{}", INSTANCE.p2p);
		LOGGER.info("最后一次选择目录：{}", INSTANCE.lastPath);
	}
	
	/**
	 * 设置下载目录
	 */
	public static final void setPath(String path) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.path = path;
		configRepository.updateConfig(DOWNLOAD_PATH, path);
	}
	
	/**
	 * 下载目录：如果文件路径存在返回文件路径，如果不存在获取user.dir路径+文件路径
	 */
	public static final String getPath() {
		String path = INSTANCE.path;
		final File file = new File(path);
		if(file.exists()) {
			return path;
		}
		path = System.getProperty("user.dir") + path;
		FileUtils.buildFolder(path, false);
		return path;
	}

	/**
	 * 获取下载目录：下载目录+文件名称
	 */
	public static final String getPath(String fileName) throws DownloadException {
		if(StringUtils.isEmpty(fileName)) {
			throw new DownloadException("无效的下载路径：" + fileName);
		}
		return FileUtils.file(getPath(), fileName);
	}
	
	/**
	 * 设置下载任务数量
	 */
	public static final void setSize(Integer size) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.size = size;
		configRepository.updateConfig(DOWNLOAD_SIZE, String.valueOf(size));
	}

	/**
	 * 下载任务数量
	 */
	public static final Integer getSize() {
		return INSTANCE.size;
	}
	
	/**
	 * 设置下载速度（单个）（KB）
	 */
	public static final void setBuffer(Integer buffer) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.buffer = buffer;
		configRepository.updateConfig(DOWNLOAD_BUFFER, String.valueOf(buffer));
	}
	
	/**
	 * 下载速度（单个）（KB）
	 */
	public static final Integer getBuffer() {
		return INSTANCE.buffer;
	}

	/**
	 * 下载速度（单个）（B）
	 */
	public static final Integer getBufferByte() {
		return INSTANCE.buffer * 1024;
	}
	
	/**
	 * 磁盘缓存（单个）（MB）
	 */
	public static final void setMemoryBuffer(Integer memoryBuffer) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.memoryBuffer = memoryBuffer;
		configRepository.updateConfig(DOWNLOAD_MEMORY_BUFFER, String.valueOf(memoryBuffer));
	}

	/**
	 * 磁盘缓存（单个）（MB）
	 */
	public static final Integer getMemoryBuffer() {
		return INSTANCE.memoryBuffer;
	}

	/**
	 * 磁盘缓存（单个）（B）
	 */
	public static final Integer getMemoryBufferByte() {
		return INSTANCE.memoryBuffer * 1024 * 1024;
	}
	
	/**
	 * 磁盘缓存（单个-Peer）（B）
	 */
	public static final Integer getPeerMemoryBufferByte() {
		return INSTANCE.memoryBuffer * 1024 * 1024 / SystemConfig.getPeerDownloadSize();
	}

	/**
	 * 设置消息提示
	 */
	public static final void setNotice(Boolean notice) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.notice = notice;
		configRepository.updateConfig(DOWNLOAD_NOTICE, String.valueOf(notice));
	}

	/**
	 * 消息提示
	 */
	public static final Boolean getNotice() {
		return INSTANCE.notice;
	}
	
	/**
	 * 设置启用P2P加速
	 */
	public static final void setP2p(Boolean p2p) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.p2p = p2p;
		configRepository.updateConfig(DOWNLOAD_P2P, String.valueOf(p2p));
	}
	
	/**
	 * 启用P2P加速
	 */
	public static final Boolean getP2p() {
		return INSTANCE.p2p;
	}
	
	/**
	 * 设置最后一次选择目录
	 */
	public static final void setLastPath(String lastPath) {
		ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.lastPath = lastPath;
		configRepository.updateConfig(DOWNLOAD_P2P, lastPath);
	}
	
	/**
	 * 最后一次选择目录
	 */
	public static final String getLastPath() {
		return INSTANCE.lastPath;
	}

	/**
	 * 最后一次选择目录文件，如果没有选择默认使用下载目录
	 */
	public static final File lastPath() {
		File file = null;
		if(StringUtils.isEmpty(INSTANCE.lastPath)) {
			file = new File(getPath());
		} else {
			file = new File(INSTANCE.lastPath);
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
