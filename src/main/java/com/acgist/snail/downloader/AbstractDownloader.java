package com.acgist.snail.downloader;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * 抽象下载器
 */
public abstract class AbstractDownloader implements IDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDownloader.class);
	
	private static final long ONE_MINUTE = 1000L; // 一分钟
	
	protected boolean complete = false;
	protected TaskWrapper wrapper;

	public AbstractDownloader(TaskWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	@Override
	public TaskWrapper taskWrapper() {
		return wrapper;
	}

	@Override
	public String id() {
		return wrapper.entity().getId();
	}
	
	@Override
	public String name() {
		return wrapper.entity().getName();
	}
	
	@Override
	public void start() {
		this.wrapper.updateStatus(Status.await);
	}
	
	@Override
	public void pause() {
		this.wrapper.updateStatus(Status.pause);
	}
	
	@Override
	public void fail() {
		this.wrapper.updateStatus(Status.fail);
	}
	
	@Override
	public void delete() {
		this.pause(); // 暂停
		var entity = wrapper.entity();
		// 删除文件：注意不删除种子文件，下载时已经将种子文件拷贝到下载目录了
		FileUtils.delete(entity.getFile());
		this.wrapper.delete();
	}
	
	@Override
	public void refresh() {
	}
	
	@Override
	public void complete() {
		this.wrapper.updateStatus(Status.complete);
	}
	
	@Override
	public void run() {
		var entity = this.wrapper.entity();
		LOGGER.info("开始下载：{}", entity.getName());
		entity.setStatus(Status.download);
		this.open();
		try {
			this.download();
		} catch (IOException e) {
			LOGGER.error("下载异常", e);
		}
		this.release();
		if(complete) {
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
	
	/**
	 * 下载中
	 */
	protected boolean downloading() {
		var entity = this.wrapper.entity();
		return entity.getStatus() == Status.download;
	}

}
