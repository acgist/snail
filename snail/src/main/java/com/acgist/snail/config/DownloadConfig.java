package com.acgist.snail.config;

import java.io.File;

import com.acgist.snail.context.EntityContext;
import com.acgist.snail.context.TaskContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 下载配置
 * 
 * @author acgist
 */
public final class DownloadConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadConfig.class);
	
	private static final DownloadConfig INSTANCE = new DownloadConfig();
	
	public static final DownloadConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 下载配置文件：{@value}
	 */
	private static final String DOWNLOAD_CONFIG = "/config/download.properties";
	/**
	 * 下载速度和上传速度的比例：{@value}
	 * 比例计算公式 = {@link #downloadBufferByte} / {@link #uploadBufferByte}
	 */
	private static final int DOWNLOAD_UPLOAD_SCALE = 4;
	/**
	 * 下载目录配置名称：{@value}
	 * 
	 * @see #path
	 */
	private static final String DOWNLOAD_PATH = "acgist.download.path";
	/**
	 * 下载数量配置名称：{@value}
	 * 
	 * @see #size
	 */
	private static final String DOWNLOAD_SIZE = "acgist.download.size";
	/**
	 * 消息提示配置名称：{@value}
	 * 
	 * @see #notice
	 */
	private static final String DOWNLOAD_NOTICE = "acgist.download.notice";
	/**
	 * 删除文件配置名称：{@value}
	 * 
	 * @see #delete
	 */
	private static final String DOWNLOAD_DELETE = "acgist.download.delete";
	/**
	 * 下载速度（单个）（KB）配置名称：{@value}
	 * 
	 * @see #buffer
	 */
	private static final String DOWNLOAD_BUFFER = "acgist.download.buffer";
	/**
	 * 最后一次选择目录配置名称：{@value}
	 * 
	 * @see #lastPath
	 */
	private static final String DOWNLOAD_LAST_PATH = "acgist.download.last.path";
	/**
	 * 磁盘缓存（单个）（MB）配置名称：{@value}
	 * 
	 * @see #memoryBuffer
	 */
	private static final String DOWNLOAD_MEMORY_BUFFER = "acgist.download.memory.buffer";
	
	static {
		INSTANCE.initFromProperties();
		INSTANCE.initFromEntity();
		INSTANCE.refreshUploadDownloadBuffer();
		INSTANCE.refreshMemoryBuffer();
		INSTANCE.logger();
		INSTANCE.release();
	}
	
	private DownloadConfig() {
		super(DOWNLOAD_CONFIG);
	}
	
	/**
	 * 下载目录
	 */
	private String path;
	/**
	 * 下载数量
	 */
	private int size;
	/**
	 * 消息提示
	 */
	private boolean notice;
	/**
	 * 删除文件
	 */
	private boolean delete;
	/**
	 * 下载速度（单个）（KB）
	 */
	private int buffer;
	/**
	 * 上传速度（单个）（B）
	 * 
	 * @see #buffer
	 */
	private int uploadBufferByte;
	/**
	 * 下载速度（单个）（B）
	 * 
	 * @see #buffer
	 */
	private int downloadBufferByte;
	/**
	 * 最后一次选择目录
	 */
	private String lastPath;
	/**
	 * 磁盘缓存（单个）（MB）
	 */
	private int memoryBuffer;
	/**
	 * 磁盘缓存（单个）（B）
	 * 
	 * @see #memoryBuffer
	 */
	private int memoryBufferByte;
	
	/**
	 * 初始化配置：配置文件
	 */
	private void initFromProperties() {
		this.path = this.getString(DOWNLOAD_PATH);
		this.size = this.getInteger(DOWNLOAD_SIZE, 4);
		this.notice = this.getBoolean(DOWNLOAD_NOTICE, true);
		this.delete = this.getBoolean(DOWNLOAD_DELETE, false);
		this.buffer = this.getInteger(DOWNLOAD_BUFFER, 1024);
		this.lastPath = this.getString(DOWNLOAD_LAST_PATH);
		this.memoryBuffer = this.getInteger(DOWNLOAD_MEMORY_BUFFER, 8);
	}
	
	/**
	 * 初始化配置：实体文件
	 */
	private void initFromEntity() {
		final EntityContext entityContext = EntityContext.getInstance();
		this.path = this.getString(entityContext.findConfig(DOWNLOAD_PATH), this.path);
		this.size = this.getInteger(entityContext.findConfig(DOWNLOAD_SIZE), this.size);
		this.notice = this.getBoolean(entityContext.findConfig(DOWNLOAD_NOTICE), this.notice);
		this.delete = this.getBoolean(entityContext.findConfig(DOWNLOAD_DELETE), this.delete);
		this.buffer = this.getInteger(entityContext.findConfig(DOWNLOAD_BUFFER), this.buffer);
		this.lastPath = this.getString(entityContext.findConfig(DOWNLOAD_LAST_PATH), this.lastPath);
		this.memoryBuffer = this.getInteger(entityContext.findConfig(DOWNLOAD_MEMORY_BUFFER), this.memoryBuffer);
	}
	
	/**
	 * 记录日志
	 */
	private void logger() {
		LOGGER.debug("下载目录：{}", this.path);
		LOGGER.debug("下载数量：{}", this.size);
		LOGGER.debug("消息提示：{}", this.notice);
		LOGGER.debug("删除文件：{}", this.delete);
		LOGGER.debug("下载速度（单个）（KB）：{}", this.buffer);
		LOGGER.debug("最后一次选择目录：{}", this.lastPath);
		LOGGER.debug("磁盘缓存（单个）（MB）：{}", this.memoryBuffer);
	}
	
	/**
	 * @param path 下载目录
	 */
	public static final void setPath(String path) {
		if(StringUtils.equals(INSTANCE.path, path)) {
			return;
		}
		INSTANCE.path = path;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_PATH, path);
	}
	
	/**
	 * 下载目录存在：下载目录
	 * 下载目录无效：用户工作目录 + 下载目录
	 * 
	 * @return 下载目录
	 * 
	 * @see FileUtils#userDir(String)
	 */
	public static final String getPath() {
		String path = INSTANCE.path;
		final File file = new File(path);
		if(file.exists()) {
			return path;
		}
		path = FileUtils.userDir(path);
		FileUtils.buildFolder(path);
		return path;
	}

	/**
	 * @param fileName 文件名称
	 * 
	 * @return 下载目录中的文件路径
	 */
	public static final String getPath(String fileName) {
		if(StringUtils.isEmpty(fileName)) {
			throw new IllegalArgumentException("文件名称格式错误：" + fileName);
		}
		return FileUtils.file(getPath(), fileName);
	}
	
	/**
	 * @param size 下载数量
	 */
	public static final void setSize(int size) {
		if(INSTANCE.size == size) {
			return;
		}
		INSTANCE.size = size;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_SIZE, String.valueOf(size));
		TaskContext.getInstance().refresh();
	}

	/**
	 * @return 下载数量
	 */
	public static final int getSize() {
		return INSTANCE.size;
	}
	
	/**
	 * @param notice 是否提示消息
	 */
	public static final void setNotice(boolean notice) {
		if(INSTANCE.notice == notice) {
			return;
		}
		INSTANCE.notice = notice;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_NOTICE, String.valueOf(notice));
	}

	/**
	 * @return 是否提示消息
	 */
	public static final boolean getNotice() {
		return INSTANCE.notice;
	}
	
	/**
	 * @param delete 是否删除文件
	 */
	public static final void setDelete(boolean delete) {
		if(INSTANCE.delete == delete) {
			return;
		}
		INSTANCE.delete = delete;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_DELETE, String.valueOf(delete));
	}
	
	/**
	 * @return 是否删除文件
	 */
	public static final boolean getDelete() {
		return INSTANCE.delete;
	}
	
	/**
	 * @param buffer 下载速度（单个）（KB）
	 */
	public static final void setBuffer(int buffer) {
		if(INSTANCE.buffer == buffer) {
			return;
		}
		INSTANCE.buffer = buffer;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_BUFFER, String.valueOf(buffer));
		INSTANCE.refreshUploadDownloadBuffer();
	}
	
	/**
	 * @return 下载速度（单个）（KB）
	 */
	public static final int getBuffer() {
		return INSTANCE.buffer;
	}
	
	/**
	 * @return 上传速度（单个）（B）
	 */
	public static final int getUploadBufferByte() {
		return INSTANCE.uploadBufferByte;
	}
	
	/**
	 * @return 下载速度（单个）（B）
	 */
	public static final int getDownloadBufferByte() {
		return INSTANCE.downloadBufferByte;
	}
	
	/**
	 * 刷新下载速度和上传速度
	 */
	private void refreshUploadDownloadBuffer() {
		this.downloadBufferByte = this.buffer * SystemConfig.ONE_KB;
		this.uploadBufferByte = this.downloadBufferByte / DOWNLOAD_UPLOAD_SCALE;
	}
	
	/**
	 * @param lastPath 最后一次选择目录
	 */
	public static final void setLastPath(String lastPath) {
		if(StringUtils.equals(INSTANCE.lastPath, lastPath)) {
			return;
		}
		INSTANCE.lastPath = lastPath;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_LAST_PATH, lastPath);
	}
	
	/**
	 * 如果最后一次选择目录为空返回下载目录
	 * 
	 * @return 最后一次选择目录
	 * 
	 * @see #getPath()
	 */
	public static final String getLastPath() {
		if(StringUtils.isEmpty(INSTANCE.lastPath)) {
			return getPath();
		} else {
			return INSTANCE.lastPath;
		}
	}
	
	/**
	 * @return 最后一次选择目录文件
	 */
	public static final File getLastPathFile() {
		return new File(getLastPath());
	}
	
	/**
	 * @param memoryBuffer 磁盘缓存（单个）（MB）
	 */
	public static final void setMemoryBuffer(int memoryBuffer) {
		if(INSTANCE.memoryBuffer == memoryBuffer) {
			return;
		}
		INSTANCE.memoryBuffer = memoryBuffer;
		EntityContext.getInstance().mergeConfig(DOWNLOAD_MEMORY_BUFFER, String.valueOf(memoryBuffer));
		INSTANCE.refreshMemoryBuffer();
	}

	/**
	 * @return 磁盘缓存（单个）（MB）
	 */
	public static final int getMemoryBuffer() {
		return INSTANCE.memoryBuffer;
	}

	/**
	 * @return 磁盘缓存（单个）（B）
	 */
	public static final int getMemoryBufferByte() {
		return INSTANCE.memoryBufferByte;
	}
	
	/**
	 * @param fileSize 默认文件大小（B）
	 * 
	 * @return 文件磁盘缓存（单个）（B）
	 */
	public static final int getMemoryBufferByte(final long fileSize) {
		final int bufferSize = getMemoryBufferByte();
		if(fileSize > 0L) {
			if(bufferSize > fileSize) {
				// 如果文件大小小于磁盘缓存直接使用文件大小作为缓存大小
				return (int) fileSize;
			} else {
				return bufferSize;
			}
		}
		return bufferSize;
	}
	
	/**
	 * 刷新磁盘缓存
	 */
	private void refreshMemoryBuffer() {
		this.memoryBufferByte = this.memoryBuffer * SystemConfig.ONE_MB;
	}
	
}
