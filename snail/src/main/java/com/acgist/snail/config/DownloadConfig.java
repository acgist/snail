package com.acgist.snail.config;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.impl.ConfigRepository;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>下载配置</p>
 * <p>默认加载配置文件配置，如果数据库存在相同配置，则使用数据库配置覆盖。</p>
 * 
 * @author acgist
 */
public final class DownloadConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadConfig.class);
	
	/**
	 * <p>单例对象</p>
	 */
	private static final DownloadConfig INSTANCE = new DownloadConfig();
	
	/**
	 * <p>获取单例对象</p>
	 * 
	 * @return 单例对象
	 */
	public static final DownloadConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>配置文件：{@value}</p>
	 */
	private static final String DOWNLOAD_CONFIG = "/config/download.properties";
	/**
	 * <p>下载速度和上传速度的比例：{@value}</p>
	 * <p>比例={@link #downloadBufferByte}/{@link #uploadBufferByte}</p>
	 */
	private static final int DOWNLOAD_UPLOAD_SCALE = 4;
	/**
	 * <p>下载目录配置名称：{@value}</p>
	 * 
	 * @see #path
	 */
	private static final String DOWNLOAD_PATH = "acgist.download.path";
	/**
	 * <p>下载数量配置名称：{@value}</p>
	 * 
	 * @see #size
	 */
	private static final String DOWNLOAD_SIZE = "acgist.download.size";
	/**
	 * <p>消息提示配置名称：{@value}</p>
	 * 
	 * @see #notice
	 */
	private static final String DOWNLOAD_NOTICE = "acgist.download.notice";
	/**
	 * <p>下载速度（单个）（KB）配置名称：{@value}</p>
	 * 
	 * @see #buffer
	 */
	private static final String DOWNLOAD_BUFFER = "acgist.download.buffer";
	/**
	 * <p>最后一次选择目录配置名称：{@value}</p>
	 * 
	 * @see #lastPath
	 */
	private static final String DOWNLOAD_LAST_PATH = "acgist.download.last.path";
	/**
	 * <p>磁盘缓存（单个）（MB）配置名称：{@value}</p>
	 * 
	 * @see #memoryBuffer
	 */
	private static final String DOWNLOAD_MEMORY_BUFFER = "acgist.download.memory.buffer";
	
	static {
		LOGGER.info("初始化下载配置：{}", DOWNLOAD_CONFIG);
		INSTANCE.initFromProperties();
		INSTANCE.initFromDatabase();
		INSTANCE.refreshUploadDownloadBuffer();
		INSTANCE.refreshMemoryBuffer();
		INSTANCE.logger();
	}
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private DownloadConfig() {
		super(DOWNLOAD_CONFIG);
	}
	
	/**
	 * <p>下载目录</p>
	 */
	private String path;
	/**
	 * <p>下载数量</p>
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
	 * <p>上传速度（单个）（B）</p>
	 * <p>缓存：防止重复计算</p>
	 */
	private int uploadBufferByte;
	/**
	 * <p>下载速度（单个）（B）</p>
	 * <p>缓存：防止重复计算</p>
	 */
	private int downloadBufferByte;
	/**
	 * <p>磁盘缓存（单个）（B）</p>
	 * <p>缓存：防止重复计算</p>
	 */
	private int memoryBufferByte;
	
	/**
	 * <p>初始化配置：配置文件</p>
	 */
	private void initFromProperties() {
		this.path = this.getString(DOWNLOAD_PATH);
		this.size = this.getInteger(DOWNLOAD_SIZE, 4);
		this.buffer = this.getInteger(DOWNLOAD_BUFFER, 1024);
		this.notice = this.getBoolean(DOWNLOAD_NOTICE, true);
		this.lastPath = this.getString(DOWNLOAD_LAST_PATH);
		this.memoryBuffer = this.getInteger(DOWNLOAD_MEMORY_BUFFER, 8);
	}
	
	/**
	 * <p>初始化配置：数据库</p>
	 */
	private void initFromDatabase() {
		final ConfigRepository configRepository = new ConfigRepository();
		ConfigEntity entity = null;
		entity = configRepository.findName(DOWNLOAD_PATH);
		this.path = this.getString(entity, this.path);
		entity = configRepository.findName(DOWNLOAD_SIZE);
		this.size = this.getInteger(entity, this.size);
		entity = configRepository.findName(DOWNLOAD_NOTICE);
		this.notice = this.getBoolean(entity, this.notice);
		entity = configRepository.findName(DOWNLOAD_BUFFER);
		this.buffer = this.getInteger(entity, this.buffer);
		entity = configRepository.findName(DOWNLOAD_LAST_PATH);
		this.lastPath = this.getString(entity, this.lastPath);
		entity = configRepository.findName(DOWNLOAD_MEMORY_BUFFER);
		this.memoryBuffer = this.getInteger(entity, this.memoryBuffer);
	}
	
	/**
	 * <p>记录日志</p>
	 */
	private void logger() {
		LOGGER.info("下载目录：{}", this.path);
		LOGGER.info("下载数量：{}", this.size);
		LOGGER.info("消息提示：{}", this.notice);
		LOGGER.info("下载速度（单个）（KB）：{}", this.buffer);
		LOGGER.info("最后一次选择目录：{}", this.lastPath);
		LOGGER.info("磁盘缓存（单个）（MB）：{}", this.memoryBuffer);
	}
	
	/**
	 * <p>设置下载目录路径</p>
	 * 
	 * @param path 下载目录路径
	 */
	public static final void setPath(String path) {
		if(StringUtils.equals(INSTANCE.path, path)) {
			// 忽略没有修改
			return;
		}
		INSTANCE.path = path;
		final ConfigRepository configRepository = new ConfigRepository();
		configRepository.merge(DOWNLOAD_PATH, path);
	}
	
	/**
	 * <p>获取下载目录路径</p>
	 * <p>下载目录存在：返回下载目录路径</p>
	 * <p>下载目录不在：返回{@code user.dir}路径 + 下载目录路径</p>
	 * 
	 * @return 下载目录路径
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
	 * <p>获取下载目录下文件路径</p>
	 * 
	 * @param fileName 文件名称
	 * 
	 * @return 文件路径
	 */
	public static final String getPath(String fileName) {
		if(StringUtils.isEmpty(fileName)) {
			throw new IllegalArgumentException("文件名称格式错误：" + fileName);
		}
		return FileUtils.file(getPath(), fileName);
	}
	
	/**
	 * <p>设置下载数量</p>
	 * 
	 * @param size 下载数量
	 */
	public static final void setSize(int size) {
		if(INSTANCE.size == size) {
			// 忽略没有修改
			return;
		}
		INSTANCE.size = size;
		final ConfigRepository configRepository = new ConfigRepository();
		configRepository.merge(DOWNLOAD_SIZE, String.valueOf(size));
		// 刷新下载任务
		DownloaderManager.getInstance().refresh();
	}

	/**
	 * <p>获取下载数量</p>
	 * 
	 * @return 下载数量
	 */
	public static final int getSize() {
		return INSTANCE.size;
	}
	
	/**
	 * <p>设置消息提示</p>
	 * 
	 * @param notice 是否提示消息
	 */
	public static final void setNotice(boolean notice) {
		if(INSTANCE.notice == notice) {
			// 忽略没有修改
			return;
		}
		INSTANCE.notice = notice;
		final ConfigRepository configRepository = new ConfigRepository();
		configRepository.merge(DOWNLOAD_NOTICE, String.valueOf(notice));
	}

	/**
	 * <p>获取消息提示</p>
	 * 
	 * @return 是否提示消息
	 */
	public static final boolean getNotice() {
		return INSTANCE.notice;
	}
	
	/**
	 * <p>设置下载速度（单个）（KB）</p>
	 * 
	 * @param buffer 下载速度
	 */
	public static final void setBuffer(int buffer) {
		if(INSTANCE.buffer == buffer) {
			// 忽略没有修改
			return;
		}
		INSTANCE.buffer = buffer;
		final ConfigRepository configRepository = new ConfigRepository();
		configRepository.merge(DOWNLOAD_BUFFER, String.valueOf(buffer));
		// 刷新下载速度和上传速度
		INSTANCE.refreshUploadDownloadBuffer();
	}
	
	/**
	 * <p>获取下载速度（单个）（KB）</p>
	 * 
	 * @return 下载速度
	 */
	public static final int getBuffer() {
		return INSTANCE.buffer;
	}
	
	/**
	 * <p>获取上传速度（单个）（B）</p>
	 * 
	 * @return 上传速度
	 */
	public static final int getUploadBufferByte() {
		return INSTANCE.uploadBufferByte;
	}
	
	/**
	 * <p>获取下载速度（单个）（B）</p>
	 * 
	 * @return 下载速度
	 */
	public static final int getDownloadBufferByte() {
		return INSTANCE.downloadBufferByte;
	}
	
	/**
	 * <p>刷新下载速度和上传速度</p>
	 */
	private void refreshUploadDownloadBuffer() {
		this.downloadBufferByte = this.buffer * SystemConfig.ONE_KB;
		this.uploadBufferByte = this.downloadBufferByte / DOWNLOAD_UPLOAD_SCALE;
	}
	
	/**
	 * <p>设置最后一次选择目录</p>
	 * 
	 * @param lastPath 最后一次选择目录
	 */
	public static final void setLastPath(String lastPath) {
		if(StringUtils.equals(INSTANCE.lastPath, lastPath)) {
			// 忽略没有修改
			return;
		}
		INSTANCE.lastPath = lastPath;
		final ConfigRepository configRepository = new ConfigRepository();
		configRepository.merge(DOWNLOAD_LAST_PATH, lastPath);
	}
	
	/**
	 * <p>获取最后一次选择目录</p>
	 * <p>如果最后一次选择目录为空返回下载目录</p>
	 * 
	 * @return 最后一次选择目录
	 */
	public static final String getLastPath() {
		if(StringUtils.isEmpty(INSTANCE.lastPath)) {
			return getPath();
		} else {
			return INSTANCE.lastPath;
		}
	}
	
	/**
	 * <p>获取最后一次选择目录文件</p>
	 * 
	 * @return 最后一次选择目录文件
	 */
	public static final File getLastPathFile() {
		return new File(getLastPath());
	}
	
	/**
	 * <p>设置磁盘缓存（单个）（MB）</p>
	 * 
	 * @param memoryBuffer 磁盘缓存
	 */
	public static final void setMemoryBuffer(int memoryBuffer) {
		if(INSTANCE.memoryBuffer == memoryBuffer) {
			// 忽略没有修改
			return;
		}
		INSTANCE.memoryBuffer = memoryBuffer;
		final ConfigRepository configRepository = new ConfigRepository();
		configRepository.merge(DOWNLOAD_MEMORY_BUFFER, String.valueOf(memoryBuffer));
		// 刷新磁盘缓存
		INSTANCE.refreshMemoryBuffer();
	}

	/**
	 * <p>获取磁盘缓存（单个）（MB）</p>
	 * 
	 * @return 磁盘缓存
	 */
	public static final int getMemoryBuffer() {
		return INSTANCE.memoryBuffer;
	}

	/**
	 * <p>获取磁盘缓存（单个）（B）</p>
	 * 
	 * @return 磁盘缓存
	 */
	public static final int getMemoryBufferByte() {
		return INSTANCE.memoryBufferByte;
	}
	
	/**
	 * <p>刷新磁盘缓存</p>
	 */
	private void refreshMemoryBuffer() {
		this.memoryBufferByte = this.memoryBuffer * SystemConfig.ONE_MB;
	}
	
}
