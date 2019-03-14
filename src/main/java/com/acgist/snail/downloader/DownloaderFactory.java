package com.acgist.snail.downloader;

import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.system.exception.DownloadException;

/**
 * 下载器构建
 */
public class DownloaderFactory {

	private String url;
	private TaskWrapper wrapper;
	private ProtocolManager manager = ProtocolManager.getInstance();
	
	private DownloaderFactory(String url) {
		this.url = url;
	}
	
	private DownloaderFactory(TaskEntity entity) throws DownloadException {
		this.wrapper = TaskWrapper.newInstance(entity);
	}
	
	/**
	 * 新建下载任务<br>
	 * 通过下载链接生成下载任务
	 */
	public static final DownloaderFactory newInstance(String url) {
		final DownloaderFactory builder = new DownloaderFactory(url);
		return builder;
	}

	/**
	 * 数据库加载下载任务<br>
	 * 已经存在下载任务
	 */
	public static final DownloaderFactory newInstance(TaskEntity entity) throws DownloadException {
		final DownloaderFactory builder = new DownloaderFactory(entity);
		builder.wrapper.loadDownloadSize(); // 加载已下载大小
		return builder;
	}
	
	/**
	 * 新建下载任务
	 */
	public void build() throws DownloadException {
		if(wrapper == null) {
			this.buildWrapper();
		}
		this.submit();
	}
	
	/**
	 * 获取下载任务
	 */
	public TaskWrapper wrapper() {
		return this.wrapper;
	}
	
	/**
	 * 持久化到数据库
	 */
	private void buildWrapper() throws DownloadException {
		this.wrapper = manager.build(url);
	}
	
	/**
	 * 执行下载
	 */
	private void submit() throws DownloadException {
		DownloaderManager.getInstance().submit(wrapper);
	}

}
