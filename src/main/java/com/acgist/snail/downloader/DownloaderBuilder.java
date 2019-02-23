package com.acgist.snail.downloader;

import com.acgist.snail.downloader.http.HttpDownloader;
import com.acgist.snail.module.config.FileTypeConfig.FileType;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.repository.impl.TaskRepository;

/**
 * 下载器构建
 */
public class DownloaderBuilder {

	private String path;
	private TaskWrapper wrapper;

	private DownloaderBuilder(String path) {
		this.path = path;
	}
	
	private DownloaderBuilder(TaskEntity entity) {
		this.wrapper = new TaskWrapper(entity);
	}
	
	public static final DownloaderBuilder createBuilder(String path) {
		final DownloaderBuilder builder = new DownloaderBuilder(path);
		return builder;
	}
	
	public static final DownloaderBuilder createBuilder(TaskEntity entity) {
		final DownloaderBuilder builder = new DownloaderBuilder(entity);
		return builder;
	}
	
	public void build() {
		if(wrapper == null) {
			this.buildWrapper();
		}
		this.builderDownloader();
	}
	
	/**
	 * 持久化到数据库
	 */
	private void buildWrapper() {
		TaskRepository repository = new TaskRepository();
		TaskEntity entity = new TaskEntity("test", Type.http, FileType.image, path, "", "", Status.await, 10);
		repository.save(entity);
		this.wrapper = new TaskWrapper(entity);
	}
	
	/**
	 * 执行下载
	 */
	private void builderDownloader() {
		DownloaderManager.getInstance().submit(new HttpDownloader(wrapper));
	}
	
}
