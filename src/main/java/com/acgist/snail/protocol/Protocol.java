package com.acgist.snail.protocol;

import java.io.File;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.session.TaskSession;
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

	protected final Type type; // 下载任务类型
	protected final String[] regexs; // 协议正则表达式
	
	protected String url; // 下载地址
	protected TaskEntity taskEntity; // 任务
	
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
	 */
	public abstract String name();
	
	/**
	 * 创建下载器
	 */
	public abstract IDownloader buildDownloader(TaskSession taskSession);
	
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
	 */
	public abstract boolean available();
	
	/**
	 * 构建下载
	 */
	public TaskSession build() throws DownloadException {
		final Protocol convert = convert();
		if(convert != null) {
			return convert.build();
		}
		final boolean ok = buildTaskEntity();
		if(ok) {
			persistentTaskEntity();
			return buildTaskSession();
		} else {
			return null;
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
	protected abstract boolean buildTaskEntity() throws DownloadException;
	
	/**
	 * 持久化任务
	 */
	protected void persistentTaskEntity() {
		final TaskRepository repository = new TaskRepository();
		repository.save(this.taskEntity);
	}
	
	/**
	 * 新建任务代理
	 */
	protected TaskSession buildTaskSession() throws DownloadException {
		final TaskSession taskSession = TaskSession.newInstance(this.taskEntity);
		this.clean();
		return taskSession;
	}

	/**
	 * 清理信息
	 */
	protected void clean() {
		this.url = null;
		this.taskEntity = null;
		cleanMessage();
	}
	
	/**
	 * 清理数据
	 */
	protected abstract void cleanMessage();

	/**
	 * 通过URL获取文件名称
	 */
	protected String buildFileName() {
		String fileName = FileUtils.fileNameFromUrl(this.url);
		fileName = FileUtils.fileName(fileName);
		return fileName;
	}
	
	/**
	 * 获取任务名称
	 */
	protected String buildName(String fileName) {
		String name;
		int index = fileName.lastIndexOf(".");
		if(index != -1) {
			name = fileName.substring(0, index);
		} else {
			name = fileName;
		}
		return name;
	}
	
	/**
	 * 设置下载文件地址
	 */
	protected String buildFile(String fileName) throws DownloadException {
		final String filePath = DownloadConfig.getPath(fileName);
		final File file = new File(filePath);
		if(file.exists()) {
			throw new DownloadException("下载文件已存在：" + file);
		}
		return filePath;
	}

}
