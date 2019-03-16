package com.acgist.snail.downloader;

import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.system.exception.DownloadException;

/**
 * 下载器工厂
 */
public class DownloaderFactory {

	private String url;
	private TaskSession session;
	private ProtocolManager manager = ProtocolManager.getInstance();
	
	private DownloaderFactory(String url) {
		this.url = url;
	}
	
	private DownloaderFactory(TaskEntity entity) throws DownloadException {
		this.session = TaskSession.newInstance(entity);
	}
	
	/**
	 * 新建下载任务<br>
	 * 通过下载链接生成下载任务
	 */
	public static final DownloaderFactory newInstance(String url) {
		return new DownloaderFactory(url);
	}

	/**
	 * 数据库加载下载任务<br>
	 * 已经存在下载任务
	 */
	public static final DownloaderFactory newInstance(TaskEntity entity) throws DownloadException {
		return new DownloaderFactory(entity);
	}
	
	/**
	 * 新建下载任务
	 */
	public void build() throws DownloadException {
		if(session == null) {
			this.buildTaskSession();
		}
		this.submit();
	}
	
	/**
	 * 获取下载任务
	 */
	public TaskSession task() {
		return this.session;
	}
	
	/**
	 * 持久化到数据库
	 */
	private void buildTaskSession() throws DownloadException {
		this.session = manager.build(url);
	}
	
	/**
	 * 执行下载
	 */
	private void submit() throws DownloadException {
		DownloaderManager.getInstance().submit(session);
	}

}
