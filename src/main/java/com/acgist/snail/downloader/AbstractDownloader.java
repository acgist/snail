package com.acgist.snail.downloader;

import java.awt.TrayIcon.MessageType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.menu.TrayMenu;
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
	
	protected boolean running = false; // 下载中
	protected boolean complete = false; // 下载完成
	protected TaskWrapper wrapper;

	public AbstractDownloader(TaskWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	@Override
	public TaskWrapper wrapper() {
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
		fail(null);
	}
	
	@Override
	public void fail(String message) {
		this.wrapper.updateStatus(Status.fail);
		StringBuilder noticeMessage = new StringBuilder();
		noticeMessage.append(name())
			.append("下载失败，失败原因：");
		if(message != null) {
			noticeMessage.append(message);
		} else {
			noticeMessage.append("未知错误");
		}
		TrayMenu.getInstance().notice("下载失败", noticeMessage.toString(), MessageType.WARNING);
	}
	
	@Override
	public void delete() {
		this.pause(); // 暂停
		ThreadUtils.timeout(5000, () -> { // 等待下载线程结束
			return !this.running;
		});
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
		if(complete) {
			this.wrapper.updateStatus(Status.complete);
			TrayMenu.getInstance().notice("下载完成", name() + "已经下载完成");
		}
	}
	
	@Override
	public void run() {
		if(wrapper.download()) { // 任务已经处于下载中直接跳过，防止多次点击暂停开始导致后面线程阻塞导致不能下载其他任务
			LOGGER.info("任务已经在下载中，停止执行：{}", name());
			return;
		}
		synchronized (wrapper) {
			var entity = this.wrapper.entity();
			if(wrapper.await()) {
				LOGGER.info("开始下载：{}", name());
				running = true; // 标记开始下载
				entity.setStatus(Status.download);
				this.open();
				try {
					this.download();
				} catch (Exception e) {
					fail(e.getMessage());
					LOGGER.error("下载异常", e);
				}
				this.release();
				this.complete();
				running = false;
			}
		}
	}
	
	/**
	 * 下载统计
	 */
	protected void statistical(long size) {
		this.wrapper.statistical(size);
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
