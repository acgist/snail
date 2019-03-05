package com.acgist.snail.downloader;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * 抽象下载器
 */
public abstract class AbstractDownloader implements IDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDownloader.class);
	
	private static final long ONE_MINUTE = 1000L; // 一分钟
	
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
		TaskRepository repository = new TaskRepository();
		this.wrapper.setStatus(Status.await);
		repository.update(this.wrapper.getEntity());
	}
	
	@Override
	public void pause() {
		TaskRepository repository = new TaskRepository();
		this.wrapper.setStatus(Status.pause);
		repository.update(this.wrapper.getEntity());
	}
	
	@Override
	public void fail() {
		TaskRepository repository = new TaskRepository();
		this.wrapper.setStatus(Status.fail);
		repository.update(this.wrapper.getEntity());
	}
	
	@Override
	public void delete() {
		this.pause(); // 暂停
		// 删除文件：注意不删除种子文件，下载时已经将种子文件拷贝到下载目录了
		FileUtils.delete(wrapper.getFile());
		TaskRepository repository = new TaskRepository();
		// 删除任务
		repository.delete(wrapper.getId());
	}
	
	@Override
	public void refresh() {
	}
	
	@Override
	public void complete() {
		TaskRepository repository = new TaskRepository();
		this.wrapper.setStatus(Status.complete);
		repository.update(this.wrapper.getEntity());
	}
	
	@Override
	public void run() {
		LOGGER.info("开始下载：{}", this.wrapper.getName());
		this.wrapper.setStatus(Status.download);
		this.open();
		boolean ok = true;
		try {
			this.download();
		} catch (IOException e) {
			ok = false;
			LOGGER.error("下载异常", e);
		}
		this.release();
		if(ok) {
			this.complete();
		}
	}
	
	/**
	 * 休眠：限速
	 */
	protected void yield(long time) {
		if(time >= ONE_MINUTE) {
			return;
		}
		ThreadUtils.sleep(ONE_MINUTE - time);
	}
	
}
