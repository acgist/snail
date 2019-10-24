package com.acgist.snail.system.config;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.impl.ConfigRepository;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>下载配置</p>
 * <p>默认从配置文件加载，如果数据有配置则使用数据库配置覆盖。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DownloadConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadConfig.class);
	
	private static final DownloadConfig INSTANCE = new DownloadConfig();
	
	private static final String DOWNLOAD_CONFIG = "/config/download.properties";
	
	/**
	 * 上传速度和下载速度比例：上传速度 = 下载速度 / 比例
	 */
	private static final int UPLOAD_DOWNLOAD_SCALE = 5;
	
	private static final String DOWNLOAD_PATH = "acgist.download.path";
	private static final String DOWNLOAD_SIZE = "acgist.download.size";
	private static final String DOWNLOAD_NOTICE = "acgist.download.notice";
	private static final String DOWNLOAD_BUFFER = "acgist.download.buffer";
	private static final String DOWNLOAD_LAST_PATH = "acgist.download.last.path";
	private static final String DOWNLOAD_MEMORY_BUFFER = "acgist.download.memory.buffer";
	
	static {
		LOGGER.info("初始化下载配置");
		INSTANCE.initFromProperties();
		INSTANCE.initFromDatabase();
		buildDownloadUploadBuffer();
		INSTANCE.logger();
	}
	
	private DownloadConfig() {
		super(DOWNLOAD_CONFIG);
	}
	
	public static final DownloadConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 下载目录
	 */
	private String path;
	/**
	 * 下载任务数量
	 */
	private int size;
	/**
	 * 消息提示
	 */
	private boolean notice;
	/**
	 * 下载速度（单个）（KB）
	 */
	private int buffer;
	/**
	 * 最后一次选择目录
	 */
	private String lastPath;
	/**
	 * 磁盘缓存（单个）（MB）
	 */
	private int memoryBuffer;
	/**
	 * 上传速度（B）
	 */
	private int uploadBufferByte;
	/**
	 * 下载速度（B）
	 */
	private int downloadBufferByte;
	
	/**
	 * 配置文件加载
	 */
	private void initFromProperties() {
		this.path = getString(DOWNLOAD_PATH);
		this.size = getInteger(DOWNLOAD_SIZE, 4);
		this.buffer = getInteger(DOWNLOAD_BUFFER, 1024);
		this.notice = getBoolean(DOWNLOAD_NOTICE, true);
		this.lastPath = getString(DOWNLOAD_LAST_PATH);
		this.memoryBuffer = getInteger(DOWNLOAD_MEMORY_BUFFER, 8);
	}
	
	/**
	 * 数据库配置加载
	 */
	private void initFromDatabase() {
		final ConfigRepository configRepository = new ConfigRepository();
		ConfigEntity entity = null;
		entity = configRepository.findName(DOWNLOAD_PATH);
		this.path = getString(entity, this.path);
		entity = configRepository.findName(DOWNLOAD_SIZE);
		this.size = getInteger(entity, this.size);
		entity = configRepository.findName(DOWNLOAD_NOTICE);
		this.notice = getBoolean(entity, this.notice);
		entity = configRepository.findName(DOWNLOAD_BUFFER);
		this.buffer = getInteger(entity, this.buffer);
		entity = configRepository.findName(DOWNLOAD_LAST_PATH);
		this.lastPath = getString(entity, this.lastPath);
		entity = configRepository.findName(DOWNLOAD_MEMORY_BUFFER);
		this.memoryBuffer = getInteger(entity, this.memoryBuffer);
	}
	
	/**
	 * 日志
	 */
	private void logger() {
		LOGGER.info("下载目录：{}", this.path);
		LOGGER.info("下载任务数量：{}", this.size);
		LOGGER.info("消息提示：{}", this.notice);
		LOGGER.info("下载速度（单个）（KB）：{}", this.buffer);
		LOGGER.info("最后一次选择目录：{}", this.lastPath);
		LOGGER.info("磁盘缓存（单个）（MB）：{}", this.memoryBuffer);
	}
	
	/**
	 * 设置下载目录
	 */
	public static final void setPath(String path) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.path = path;
		configRepository.merge(DOWNLOAD_PATH, path);
	}
	
	/**
	 * 下载目录：如果文件路径存在返回文件路径，如果不存在获取user.dir路径+文件路径。
	 */
	public static final String getPath() {
		String path = INSTANCE.path;
		final File file = new File(path);
		if(file.exists()) {
			return path;
		}
		path = SystemConfig.userDir(path);
		FileUtils.buildFolder(path, false);
		return path;
	}

	/**
	 * 下载目录：下载目录+文件名称
	 */
	public static final String getPath(String fileName) {
		if(StringUtils.isEmpty(fileName)) {
			throw new ArgumentException("文件名称格式错误：" + fileName);
		}
		return FileUtils.file(getPath(), fileName);
	}
	
	/**
	 * 设置下载任务数量
	 */
	public static final void setSize(int size) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.size = size;
		configRepository.merge(DOWNLOAD_SIZE, String.valueOf(size));
	}

	/**
	 * 下载任务数量
	 */
	public static final int getSize() {
		return INSTANCE.size;
	}
	
	/**
	 * 设置消息提示
	 */
	public static final void setNotice(boolean notice) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.notice = notice;
		configRepository.merge(DOWNLOAD_NOTICE, String.valueOf(notice));
	}

	/**
	 * 消息提示
	 */
	public static final boolean getNotice() {
		return INSTANCE.notice;
	}
	
	/**
	 * 设置下载速度（单个）（KB）
	 */
	public static final void setBuffer(int buffer) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.buffer = buffer;
		configRepository.merge(DOWNLOAD_BUFFER, String.valueOf(buffer));
		buildDownloadUploadBuffer();
	}
	
	/**
	 * 下载速度（单个）（KB）
	 */
	public static final int getBuffer() {
		return INSTANCE.buffer;
	}

	/**
	 * 设置下载速度和上传速度
	 */
	private static final void buildDownloadUploadBuffer() {
		INSTANCE.downloadBufferByte = INSTANCE.buffer * SystemConfig.DATA_SCALE;
		INSTANCE.uploadBufferByte = INSTANCE.downloadBufferByte / UPLOAD_DOWNLOAD_SCALE;
	}
	
	/**
	 * 下载速度（单个）（B）
	 */
	public static final int getDownloadBufferByte() {
		return INSTANCE.downloadBufferByte;
	}
	
	/**
	 * 上传速度（单个）（B）
	 */
	public static final int getUploadBufferByte() {
		return INSTANCE.uploadBufferByte;
	}
	
	/**
	 * 设置最后一次选择目录
	 */
	public static final void setLastPath(String lastPath) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.lastPath = lastPath;
		configRepository.merge(DOWNLOAD_LAST_PATH, lastPath);
	}
	
	/**
	 * 最后一次选择目录
	 */
	public static final String getLastPath() {
		if(StringUtils.isEmpty(INSTANCE.lastPath)) {
			return getPath();
		} else {
			return INSTANCE.lastPath;
		}
	}
	
	/**
	 * 最后一次选择目录文件：如果不存在选择默认使用下载目录
	 */
	public static final File getLastPathFile() {
		return new File(getLastPath());
	}
	
	/**
	 * 设置磁盘缓存（单个）（MB）
	 */
	public static final void setMemoryBuffer(int memoryBuffer) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.memoryBuffer = memoryBuffer;
		configRepository.merge(DOWNLOAD_MEMORY_BUFFER, String.valueOf(memoryBuffer));
	}

	/**
	 * 磁盘缓存（单个）（MB）
	 */
	public static final int getMemoryBuffer() {
		return INSTANCE.memoryBuffer;
	}

	/**
	 * 磁盘缓存（单个）（B）
	 */
	public static final int getMemoryBufferByte() {
		return INSTANCE.memoryBuffer * SystemConfig.ONE_MB;
	}
	
}
