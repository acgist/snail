package com.acgist.snail.downloader;

import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.utils.FileUtils;

/**
 * 抽象下载器
 */
public abstract class AbstractDownloader implements IDownloader {

	protected TaskWrapper wrapper;

	public AbstractDownloader(TaskWrapper wrapper) {
		this.wrapper = wrapper;
	}

	@Override
	public String id() {
		return wrapper.getId();
	}
	
	@Override
	public TaskWrapper taskWrapper() {
		return wrapper;
	}
	
	@Override
	public String name() {
		return wrapper.getName();
	}
	
	@Override
	public void start() {
		this.wrapper.setStatus(Status.await);
	}
	
	@Override
	public void pause() {
		this.wrapper.setStatus(Status.pause);
	}
	
	@Override
	public void delete() {
		this.pause(); // 暂停
		// 删除文件：注意不删除种子文件，下载时已经将种子文件拷贝到下载目录了
		FileUtils.delete(wrapper.getFile());
	}
	
}
