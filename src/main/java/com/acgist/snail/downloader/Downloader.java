package com.acgist.snail.downloader;

import java.awt.TrayIcon.MessageType;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.menu.TrayMenu;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.manager.DownloaderManager;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * 抽象下载器
 */
public abstract class Downloader implements IDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);
	
	protected boolean fail = false; // 失败状态
	protected boolean running = false; // 下载中
	protected boolean complete = false; // 下载完成
	
	private Object deleteLock = new Object();
	
	protected TaskSession taskSession;

	public Downloader(TaskSession taskSession) {
		this.taskSession = taskSession;
		taskSession.downloadSize(downloadSize()); // 加载已下载大小
	}
	
	@Override
	public boolean running() {
		return running;
	}
	
	@Override
	public TaskSession task() {
		return taskSession;
	}

	@Override
	public String id() {
		return taskSession.entity().getId();
	}
	
	@Override
	public String name() {
		return taskSession.entity().getName();
	}
	
	@Override
	public void start() {
		this.taskSession.updateStatus(Status.await);
	}
	
	@Override
	public void pause() {
		this.taskSession.updateStatus(Status.pause);
	}
	
	@Override
	public void fail(String message) {
		this.fail = true;
		this.taskSession.updateStatus(Status.fail);
		StringBuilder noticeMessage = new StringBuilder();
		noticeMessage.append(name())
			.append("下载失败，失败原因：");
		if(message != null) {
			noticeMessage.append(message);
		} else {
			noticeMessage.append("未知错误");
		}
		TrayMenu.getInstance().notice("下载失败", noticeMessage.toString(), MessageType.WARNING);
		DownloaderManager.getInstance().refresh();
	}
	
	@Override
	public void delete() {
		this.pause(); // 暂停
		synchronized (this.deleteLock) {
			ThreadUtils.wait(this.deleteLock, Duration.ofSeconds(5));
		}
		TaskRepository repository = new TaskRepository();
		repository.delete(taskSession.entity());
	}
	
	@Override
	public void refresh() {
	}
	
	@Override
	public void complete() {
		if(complete) {
			this.taskSession.updateStatus(Status.complete);
			TrayMenu.getInstance().notice("下载完成", name() + "已经下载完成");
		}
	}
	
	@Override
	public long downloadSize() {
		return FileUtils.fileSize(taskSession.entity().getFile());
	}
	
	@Override
	public void run() {
		if(taskSession.download()) { // 任务已经处于下载中直接跳过，防止多次点击暂停开始导致后面线程阻塞导致不能下载其他任务
			LOGGER.info("任务已经在下载中，停止执行：{}", name());
			return;
		}
		synchronized (taskSession) {
			var entity = this.taskSession.entity();
			if(taskSession.await()) {
				LOGGER.info("开始下载：{}", name());
				fail = false; // 标记下载失败
				running = true; // 标记开始下载
				entity.setStatus(Status.download);
				this.open();
				try {
					this.download();
				} catch (Exception e) {
					fail(e.getMessage());
					LOGGER.error("下载异常", e);
				}
				this.complete();
				this.release(); // 最后释放资源
				running = false;
				this.unlockDelete();
				LOGGER.info("下载结束：{}", name());
			}
		}
	}
	
	/**
	 * 下载统计
	 */
	protected void statistics(long size) {
		this.taskSession.statistics(size);
	}
	
	/**
	 * 判断是否可以下载<br>
	 * 一下情况不能继续下载：<br>
	 * 	1.任务状态不是下载中
	 * 	2.失败标记=true
	 */
	protected boolean ok() {
		return !fail && taskSession.download();
	}
	
	/**
	 * 唤醒删除
	 * TODO：测试
	 */
	private void unlockDelete() {
		synchronized (this.deleteLock) {
			this.deleteLock.notifyAll();
		}
	}
	
}
