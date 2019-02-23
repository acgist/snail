package com.acgist.snail.downloader;

import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.utils.FileUtils;

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
	
	@Override
	public String name() {
		return task.getName();
	}
	
	@Override
	public void delete() {
		this.pause(); // 暂停
		// 删除文件
		FileUtils.delete(task.getFile());
		if(task.getType() == Type.torremt) {
			FileUtils.delete(task.getTorrent());
		}
		// 删除任务
	}
	
}
