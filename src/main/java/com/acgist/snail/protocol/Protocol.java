package com.acgist.snail.protocol;

import java.io.File;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TaskSession.Status;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/***
 * 下载协议
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Protocol {

	/**
	 * 下载类型
	 */
	public enum Type {

		/** FTP */
		ftp,
		/** HTTP */
		http,
		/** 磁力链接 */
		magnet,
		/** 迅雷链接 */
		thunder,
		/** BT */
		torrent;

	}
	
	/**
	 * 下载任务类型
	 */
	protected final Type type;
	/**
	 * 协议正则表达式
	 */
	protected final String[] regexs;
	/**
	 * 下载地址
	 */
	protected String url;
	/**
	 * 任务
	 */
	protected TaskEntity taskEntity;
	
	public Protocol(Type type, String ... regexs) {
		this.type = type;
		this.regexs = regexs;
	}

	/**
	 * 初始化
	 */
	public Protocol init(String url) {
		this.url = url.trim();
		return this;
	}
	
	public Type type() {
		return this.type;
	}
	
	/**
	 * 协议名称
	 * 
	 * @return 下载协议名称
	 */
	public abstract String name();
	
	/**
	 * 验证是否支持协议
	 */
	public boolean verify() {
		if(this.regexs == null) {
			return false;
		}
		for (String regex : this.regexs) {
			final boolean match = StringUtils.regex(this.url, regex, true);
			if(match) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 是否可用
	 * 
	 * @return 是否可用
	 */
	public abstract boolean available();
	
	/**
	 * 创建下载器
	 * 
	 * @param taskSession 任务
	 * 
	 * @return 下载器
	 */
	public abstract IDownloader buildDownloader(TaskSession taskSession);
	
	/**
	 * 创建下载任务
	 */
	public TaskSession buildTaskSession() throws DownloadException {
		final Protocol convert = convert();
		if(convert != null) {
			return convert.buildTaskSession();
		}
		boolean ok = true;
		try {
			buildTaskEntity();
			return TaskSession.newInstance(this.taskEntity);
		} catch (DownloadException e) {
			ok = false;
			throw e;
		} catch (Exception e) {
			ok = false;
			throw new DownloadException("下载异常", e);
		} finally {
			clean(ok);
		}
	}

	/**
	 * 协议转换<br>
	 * 如果返回值不为空，则使用返回的协议进行下载。
	 */
	protected Protocol convert() throws DownloadException {
		return null;
	}
	
	/**
	 * 创建下载任务
	 */
	protected void buildTaskEntity() throws DownloadException {
		this.taskEntity = new TaskEntity();
		prep();
		buildUrl();
		buildType();
		buildStatus();
		final String fileName = buildFileName();
		buildName(fileName);
		buildFile(fileName);
		buildFileType(fileName);
		buildTorrent();
		buildSize();
		done();
		persistentTaskEntity();
	}
	
	/**
	 * 预处理
	 */
	protected void prep() throws DownloadException {
	}
	
	/**
	 * 设置URL
	 */
	protected void buildUrl() throws DownloadException {
		this.taskEntity.setUrl(this.url);
	}

	/**
	 * 设置下载类型
	 */
	protected void buildType() throws DownloadException {
		this.taskEntity.setType(this.type);
	}

	/**
	 * 设置任务状态
	 */
	protected void buildStatus() throws DownloadException {
		this.taskEntity.setStatus(Status.await);
	}

	/**
	 * 获取文件名称
	 */
	protected String buildFileName() throws DownloadException {
		String fileName = FileUtils.fileNameFromUrl(this.url);
		fileName = FileUtils.fileName(fileName);
		return fileName;
	}

	/**
	 * 设置任务名称
	 */
	protected void buildName(String fileName) throws DownloadException {
		String name;
		int index = fileName.lastIndexOf(".");
		if(index != -1) {
			name = fileName.substring(0, index);
		} else {
			name = fileName;
		}
		this.taskEntity.setName(name);
	}
	
	/**
	 * 生成并设置文件、文件夹
	 */
	protected void buildFile(String fileName) throws DownloadException {
		final String filePath = DownloadConfig.getPath(fileName);
		final File file = new File(filePath);
		if(file.exists()) {
			throw new DownloadException("下载文件已存在：" + file);
		}
		this.taskEntity.setFile(filePath);
	}
	
	/**
	 * 设置任务文件类型
	 */
	protected void buildFileType(String fileName) throws DownloadException {
		this.taskEntity.setFileType(FileUtils.fileType(fileName));
	}

	/**
	 * 设置种子文件
	 */
	protected void buildTorrent() throws DownloadException {
	}
	
	/**
	 * 设置任务大小
	 */
	protected void buildSize() throws DownloadException {
	}
	
	/**
	 * 完成处理
	 */
	protected void done() throws DownloadException {
	}
	
	/**
	 * 持久化任务
	 */
	protected void persistentTaskEntity() throws DownloadException {
		final TaskRepository repository = new TaskRepository();
		repository.save(this.taskEntity);
	}
	
	/**
	 * 清理信息
	 */
	protected void clean(boolean ok) {
		this.url = null;
		this.taskEntity = null;
		cleanMessage(ok);
	}
	
	/**
	 * 清理数据
	 * 
	 * @param ok 创建状态：true-成功；false-失败；
	 */
	protected abstract void cleanMessage(boolean ok);
	
}
