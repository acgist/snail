package com.acgist.snail.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

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
	
	/**
	 * 下载配置文件
	 */
	public static final String DOWNLOAD_CONFIG = "/config/download.properties";
	/**
	 * 下载速度和上传速度的比例
	 * 比例计算公式 = {@link #downloadBufferByte} / {@link #uploadBufferByte}
	 */
	private static final int DOWNLOAD_UPLOAD_SCALE = 4;
	/**
	 * 下载目录配置名称
	 * 
	 * @see #path
	 */
	private static final String DOWNLOAD_PATH = "acgist.download.path";
	/**
	 * 下载数量配置名称
	 * 
	 * @see #size
	 */
	private static final String DOWNLOAD_SIZE = "acgist.download.size";
	/**
	 * 消息提示配置名称
	 * 
	 * @see #notice
	 */
	private static final String DOWNLOAD_NOTICE = "acgist.download.notice";
	/**
	 * 删除文件配置名称
	 * 
	 * @see #delete
	 */
	private static final String DOWNLOAD_DELETE = "acgist.download.delete";
	/**
	 * 下载速度（单个）（KB）配置名称
	 * 
	 * @see #buffer
	 */
	private static final String DOWNLOAD_BUFFER = "acgist.download.buffer";
	/**
	 * 最后一次选择目录配置名称
	 * 
	 * @see #lastPath
	 */
	private static final String DOWNLOAD_LAST_PATH = "acgist.download.last.path";
	/**
	 * 磁盘缓存（单个）（MB）配置名称
	 * 
	 * @see #memoryBuffer
	 */
	private static final String DOWNLOAD_MEMORY_BUFFER = "acgist.download.memory.buffer";
	
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
	
	private static final DownloadConfig INSTANCE = new DownloadConfig();
	
	public static final DownloadConfig getInstance() {
		return INSTANCE;
	}
	
	private DownloadConfig() {
		super(DOWNLOAD_CONFIG);
		this.init();
		this.release();
		this.refreshBuffer();
		this.refreshMemoryBuffer();
	}

//	@Override
//	protected Properties loadProperties(String path) {
//		return null;
//	}

	@Override
	public void init() {
		// 加载配置
		this.path = this.getString(DOWNLOAD_PATH);
		this.size = this.getInteger(DOWNLOAD_SIZE, 4);
		this.notice = this.getBoolean(DOWNLOAD_NOTICE, true);
		this.delete = this.getBoolean(DOWNLOAD_DELETE, false);
		this.buffer = this.getInteger(DOWNLOAD_BUFFER, 1024);
		this.lastPath = this.getString(DOWNLOAD_LAST_PATH);
		this.memoryBuffer = this.getInteger(DOWNLOAD_MEMORY_BUFFER, 8);
		// 记录日志
		LOGGER.debug("下载目录：{}", this.path);
		LOGGER.debug("下载数量：{}", this.size);
		LOGGER.debug("消息提示：{}", this.notice);
		LOGGER.debug("删除文件：{}", this.delete);
		LOGGER.debug("下载速度（单个）（KB）：{}", this.buffer);
		LOGGER.debug("最后一次选择目录：{}", this.lastPath);
		LOGGER.debug("磁盘缓存（单个）（MB）：{}", this.memoryBuffer);
	}
	
	@Override
	public void persistent() {
		final Map<String, String> data = new HashMap<>();
		data.put(DOWNLOAD_PATH, this.path);
		data.put(DOWNLOAD_SIZE, Objects.toString(this.size, "4"));
		data.put(DOWNLOAD_NOTICE, Objects.toString(this.notice, "true"));
		data.put(DOWNLOAD_DELETE, Objects.toString(this.delete, "false"));
		data.put(DOWNLOAD_BUFFER, Objects.toString(this.buffer, "1024"));
		data.put(DOWNLOAD_LAST_PATH, this.lastPath);
		data.put(DOWNLOAD_MEMORY_BUFFER, Objects.toString(this.memoryBuffer, "8"));
		this.persistent(data, DOWNLOAD_CONFIG);
	}
	
	/**
	 * @param path 下载目录
	 */
	public static final void setPath(String path) {
		if(StringUtils.equals(INSTANCE.path, path)) {
			return;
		}
		INSTANCE.path = path;
		INSTANCE.persistent();
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
		final File file = new File(INSTANCE.path);
		if(file.exists() && file.isDirectory()) {
			return INSTANCE.path;
		}
		final String path = FileUtils.userDir(INSTANCE.path);
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
		INSTANCE.persistent();
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
		INSTANCE.persistent();
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
		INSTANCE.persistent();
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
		INSTANCE.persistent();
		INSTANCE.refreshBuffer();
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
	private void refreshBuffer() {
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
		INSTANCE.persistent();
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
		}
		final File file = new File(INSTANCE.lastPath);
		if(file.exists() && file.isDirectory()) {
			return INSTANCE.lastPath;
		}
		return getPath();
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
		INSTANCE.persistent();
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
	 * @return 磁盘缓存（单个）（B）
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
