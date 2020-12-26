package com.acgist.snail.downloader;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.Snail;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskSession.Status;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>下载器</p>
 * 
 * @author acgist
 */
public abstract class Downloader implements IDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);
	
	/**
	 * <p>{@linkplain #deleteLock 删除锁}等待时间（毫秒）：{@value}</p>
	 */
	private static final int DELETE_TIMEOUT = 5000;
	
	/**
	 * <p>任务失败状态</p>
	 * <p>true-失败；false-正常；</p>
	 */
	protected volatile boolean fail = false;
	/**
	 * <p>任务完成状态</p>
	 * <p>true-已完成；false-未完成；</p>
	 */
	protected volatile boolean complete = false;
	/**
	 * <p>任务信息</p>
	 */
	protected final ITaskSession taskSession;
	/**
	 * <p>统计信息</p>
	 */
	protected final IStatisticsSession statistics;
	/**
	 * <p>删除锁</p>
	 * <p>true-任务没有下载（可以删除）</p>
	 * <p>false-任务正在下载（不能删除）：等待任务结束</p>
	 */
	private final AtomicBoolean deleteLock = new AtomicBoolean(false);
	
	/**
	 * @param taskSession 任务信息
	 */
	protected Downloader(ITaskSession taskSession) {
		this.taskSession = taskSession;
		this.statistics = taskSession.statistics();
		// 已下载大小
		final long downloadSize = FileUtils.fileSize(this.taskSession.getFile());
		this.taskSession.downloadSize(downloadSize);
		// 初始化删除锁：任务没有下载可以直接删除
		this.deleteLock.set(!this.taskSession.download());
	}
	
	@Override
	public String id() {
		return this.taskSession.getId();
	}
	
	@Override
	public String name() {
		return this.taskSession.getName();
	}
	
	@Override
	public ITaskSession taskSession() {
		return this.taskSession;
	}
	
	@Override
	public void start() {
		// 任务已经开始不修改状态
		if(this.taskSession.download()) {
			return;
		}
		// 任务已经完成不修改状态
		if(this.taskSession.complete()) {
			return;
		}
		this.updateStatus(Status.AWAIT);
	}
	
	@Override
	public void pause() {
		// 任务已经暂停不修改状态
		if(this.taskSession.pause()) {
			return;
		}
		// 任务已经完成不修改状态
		if(this.taskSession.complete()) {
			return;
		}
		this.updateStatus(Status.PAUSE);
	}
	
	@Override
	public void fail(String message) {
		this.fail = true;
		this.updateStatus(Status.FAIL);
		final StringBuilder noticeMessage = new StringBuilder();
		noticeMessage
			.append(this.name())
			.append("下载失败：");
		if(StringUtils.isEmpty(message)) {
			noticeMessage.append("未知错误");
		} else {
			noticeMessage.append(message);
		}
		GuiManager.getInstance().notice("下载失败", noticeMessage.toString(), GuiManager.MessageType.WARN);
	}
	
	@Override
	public void delete() {
		this.pause(); // 暂停任务
		this.lockDelete(); // 加锁
		this.taskSession.delete(); // 删除任务
	}
	
	@Override
	public void refresh() {
	}
	
	@Override
	public void unlockDownload() {
		Snail.getInstance().unlockDownload();
	}
	
	@Override
	public void checkComplete() {
		if(this.complete) {
			this.updateStatus(Status.COMPLETE);
			GuiManager.getInstance().notice("下载完成", "任务下载完成：" + this.name());
		}
	}
	
	@Override
	public void release() {
		this.gc();
	}
	
	@Override
	public void run() {
		// 任务已经开始下载直接跳过：防止多次点击暂停开始导致阻塞后面下载任务线程
		final String name = this.name();
		if(this.taskSession.download()) {
			LOGGER.debug("任务已经开始下载：{}", name);
			return;
		}
		synchronized (this.taskSession) {
			if(this.taskSession.await()) {
				LOGGER.info("开始下载任务：{}", name);
				this.fail = false; // 重置下载失败状态
				this.deleteLock.set(false); // 设置删除锁状态
				this.taskSession.setStatus(Status.DOWNLOAD); // 修改任务状态
				try {
					this.open();
					this.download();
				} catch (Exception e) {
					LOGGER.error("任务下载异常", e);
					this.fail(e.getMessage());
				}
				this.checkComplete(); // 检查完成状态
				this.release(); // 释放资源
				this.unlockDelete(); // 释放删除锁
				LOGGER.info("任务下载结束：{}", name);
			} else {
				LOGGER.warn("任务状态错误：{}-{}", name, this.taskSession.getStatus());
			}
		}
	}
	
	/**
	 * <dl>
	 * 	<dt>判断任务是否可以下载</dt>
	 * 	<dd>未被标记{@linkplain #fail 失败}</dd>
	 * 	<dd>未被标记{@linkplain #complete 完成}</dd>
	 * 	<dd>任务处于{@linkplain ITaskSession#download() 下载状态}</dd>
	 * </dl>
	 * 
	 * @return true-可以下载；false-不能下载；
	 */
	protected boolean downloadable() {
		return
			!this.fail &&
			!this.complete &&
			this.taskSession.download();
	}

	/**
	 * <p>添加{@linkplain #deleteLock 删除锁}</p>
	 */
	private void lockDelete() {
		if(!this.deleteLock.get()) {
			synchronized (this.deleteLock) {
				if(!this.deleteLock.get()) {
					try {
						this.deleteLock.wait(DELETE_TIMEOUT);
					} catch (InterruptedException e) {
						LOGGER.debug("线程等待异常", e);
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}
	
	/**
	 * <p>释放{@linkplain #deleteLock 删除锁}</p>
	 */
	private void unlockDelete() {
		synchronized (this.deleteLock) {
			this.deleteLock.set(true);
			this.deleteLock.notifyAll();
		}
	}
	
	/**
	 * <p>修改任务状态</p>
	 * <p>释放下载锁、修改任务状态</p>
	 * 
	 * @param status 任务状态
	 */
	private void updateStatus(Status status) {
		this.unlockDownload();
		this.taskSession.updateStatus(status);
	}
	
	/**
	 * <p>垃圾回收</p>
	 */
	private void gc() {
		LOGGER.debug("垃圾回收（GC）");
		System.gc();
	}
	
}
