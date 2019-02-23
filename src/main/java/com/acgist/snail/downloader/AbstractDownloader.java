package com.acgist.snail.downloader;

import com.acgist.snail.pojo.wrapper.TaskWrapper;

/**
 * 抽象下载器
 */
public abstract class AbstractDownloader implements IDownloader {

	protected TaskWrapper task;

	public AbstractDownloader(TaskWrapper task) {
		this.task = task;
	}

	@Override
	public String id() {
		return task.getId();
	}
	
	@Override
	public TaskWrapper task() {
		return task;
	}
	
}
