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
 * <p>默认从配置文件加载，如果数据库有配置，则使用数据库配置覆盖。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DownloadConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadConfig.class);
	
	private static final DownloadConfig INSTANCE = new DownloadConfig();
	
	private static final String DOWNLOAD_CONFIG = "/config/download.properties";
	
	/**
	 * <p>下载速度和上传速度的比例 = 下载速度 / 上传速度
	 */
	private static final int UPLOAD_DOWNLOAD_SCALE = 4;
	
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
	 * <p>下载目录</p>
	 */
	private String path;
	/**
	 * <p>下载任务数量</p>
	 */
	private int size;
	/**
	 * <p>消息提示</p>
	 */
	private boolean notice;
	/**
	 * <p>下载速度（单个）（KB）</p>
	 */
	private int buffer;
	/**
	 * <p>最后一次选择目录</p>
	 */
	private String lastPath;
	/**
	 * <p>磁盘缓存（单个）（MB）</p>
	 */
	private int memoryBuffer;
	/**
	 * <p>上传速度（B）</p>
	 */
	private int uploadBufferByte;
	/**
	 * <p>下载速度（B）</p>
	 */
	private int downloadBufferByte;
	
	/**
	 * <p>配置文件加载</p>
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
	 * <p>数据库配置加载</p>
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
	 * <p>日志</p>
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
	 * <p>设置下载目录</p>
	 */
	public static final void setPath(String path) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.path = path;
		configRepository.merge(DOWNLOAD_PATH, path);
	}
	
	/**
	 * <p>下载目录</p>
	 * <p>如果文件路径存在返回文件路径</p>
	 * <p>如果不存在获取user.dir路径 + 文件路径</p>
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
	 * <p>下载目录：下载目录 + 文件名称</p>
	 */
	public static final String getPath(String fileName) {
		if(StringUtils.isEmpty(fileName)) {
			throw new ArgumentException("文件名称格式错误：" + fileName);
		}
		return FileUtils.file(getPath(), fileName);
	}
	
	/**
	 * <p>设置下载任务数量</p>
	 */
	public static final void setSize(int size) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.size = size;
		configRepository.merge(DOWNLOAD_SIZE, String.valueOf(size));
	}

	/**
	 * <p>下载任务数量</p>
	 */
	public static final int getSize() {
		return INSTANCE.size;
	}
	
	/**
	 * <p>设置消息提示</p>
	 */
	public static final void setNotice(boolean notice) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.notice = notice;
		configRepository.merge(DOWNLOAD_NOTICE, String.valueOf(notice));
	}

	/**
	 * <p>消息提示</p>
	 */
	public static final boolean getNotice() {
		return INSTANCE.notice;
	}
	
	/**
	 * <p>设置下载速度（单个）（KB）</p>
	 */
	public static final void setBuffer(int buffer) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.buffer = buffer;
		configRepository.merge(DOWNLOAD_BUFFER, String.valueOf(buffer));
		buildDownloadUploadBuffer();
	}
	
	/**
	 * <p>下载速度（单个）（KB）</p>
	 */
	public static final int getBuffer() {
		return INSTANCE.buffer;
	}

	/**
	 * <p>设置下载速度和上传速度</p>
	 */
	private static final void buildDownloadUploadBuffer() {
		INSTANCE.downloadBufferByte = INSTANCE.buffer * SystemConfig.DATA_SCALE;
		INSTANCE.uploadBufferByte = INSTANCE.downloadBufferByte / UPLOAD_DOWNLOAD_SCALE;
	}
	
	/**
	 * <p>下载速度（单个）（B）</p>
	 */
	public static final int getDownloadBufferByte() {
		return INSTANCE.downloadBufferByte;
	}
	
	/**
	 * <p>上传速度（单个）（B）</p>
	 */
	public static final int getUploadBufferByte() {
		return INSTANCE.uploadBufferByte;
	}
	
	/**
	 * <p>设置最后一次选择目录</p>
	 */
	public static final void setLastPath(String lastPath) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.lastPath = lastPath;
		configRepository.merge(DOWNLOAD_LAST_PATH, lastPath);
	}
	
	/**
	 * <p>最后一次选择目录</p>
	 */
	public static final String getLastPath() {
		if(StringUtils.isEmpty(INSTANCE.lastPath)) {
			return getPath();
		} else {
			return INSTANCE.lastPath;
		}
	}
	
	/**
	 * <p>最后一次选择目录文件：如果不存在选择默认使用下载目录</p>
	 */
	public static final File getLastPathFile() {
		return new File(getLastPath());
	}
	
	/**
	 * <p>设置磁盘缓存（单个）（MB）</p>
	 */
	public static final void setMemoryBuffer(int memoryBuffer) {
		final ConfigRepository configRepository = new ConfigRepository();
		INSTANCE.memoryBuffer = memoryBuffer;
		configRepository.merge(DOWNLOAD_MEMORY_BUFFER, String.valueOf(memoryBuffer));
	}

	/**
	 * <p>磁盘缓存（单个）（MB）</p>
	 */
	public static final int getMemoryBuffer() {
		return INSTANCE.memoryBuffer;
	}

	/**
	 * <p>磁盘缓存（单个）（B）</p>
	 */
	public static final int getMemoryBufferByte() {
		return INSTANCE.memoryBuffer * SystemConfig.ONE_MB;
	}
	
}
