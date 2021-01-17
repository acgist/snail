package com.acgist.snail.downloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.Snail;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>下载器</p>
 * 
 * @author acgist
 */
public abstract class Downloader implements IDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);
	
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
	 * @param taskSession 任务信息
	 */
	protected Downloader(ITaskSession taskSession) {
		taskSession.buildDownloadSize();
		this.taskSession = taskSession;
		this.statistics = taskSession.statistics();
	}
	
	@Override
	public final String id() {
		return this.taskSession.getId();
	}
	
	@Override
	public final String name() {
		return this.taskSession.getName();
	}
	
	@Override
	public final ITaskSession taskSession() {
		return this.taskSession;
	}
	
	@Override
	public final boolean statusAwait() {
		return this.taskSession.statusAwait();
	}
	
	@Override
	public final boolean statusPause() {
		return this.taskSession.statusPause();
	}
	
	@Override
	public final boolean statusDownload() {
		return this.taskSession.statusDownload();
	}
	
	@Override
	public final boolean statusComplete() {
		return this.taskSession.statusComplete();
	}
	
	@Override
	public boolean statusFail() {
		return this.taskSession.statusFail();
	}
	
	@Override
	public boolean statusDelete() {
		return this.taskSession.statusDelete();
	}
	
	@Override
	public final boolean statusRunning() {
		return this.taskSession.statusRunning();
	}
	
	@Override
	public void refresh() throws DownloadException {
	}
	
	@Override
	public boolean verify() throws DownloadException {
		final boolean verify = this.taskSession.downloadFile().exists();
		if(!verify) {
			// 如果文件已删除修改已下载大小
			this.taskSession.downloadSize(0L);
		}
		return verify;
	}
	
	@Override
	public final void fail(String message) {
		this.taskSession.updateStatus(Status.FAIL);
		this.fail = true;
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
	public void unlockDownload() {
		Snail.getInstance().unlockDownload();
	}
	
	@Override
	public void release() {
		this.gc();
	}

	@Override
	public void delete() {
	}
	
	@Override
	public final void run() {
		final String name = this.name();
		if(this.statusAwait()) {
			// 验证任务状态：防止多次点击暂停开始导致阻塞后面下载任务线程
			synchronized (this.taskSession) {
				// 加锁：保证资源加载和释放原子性
				if(this.statusAwait()) {
					LOGGER.debug("开始下载任务：{}", name);
					this.fail = false; // 重置下载失败状态
					this.complete = false; // 重置下载成功状态
					this.taskSession.setStatus(Status.DOWNLOAD); // 修改任务状态：不能保存
					try {
						this.open();
						this.download();
					} catch (Exception e) {
						LOGGER.error("任务下载异常", e);
						this.fail(e.getMessage());
					}
					this.checkAndMarkComplete(); // 标记完成
					this.release(); // 释放资源
					LOGGER.debug("任务下载结束：{}", name);
				} else {
					LOGGER.warn("任务状态错误：{}-{}", name, this.taskSession.getStatus());
				}
			}
		} else {
			LOGGER.warn("任务状态错误：{}-{}", name, this.taskSession.getStatus());
		}
	}
	
	/**
	 * <dl>
	 * 	<dt>判断任务是否可以下载</dt>
	 * 	<dd>未被标记{@linkplain #fail 失败}</dd>
	 * 	<dd>未被标记{@linkplain #complete 完成}</dd>
	 * 	<dd>任务处于{@linkplain #statusDownload() 下载状态}</dd>
	 * </dl>
	 * 
	 * @return true-可以下载；false-不能下载；
	 */
	protected final boolean downloadable() {
		return
			!this.fail &&
			!this.complete &&
			this.statusDownload();
	}
	
	/**
	 * <p>标记任务完成</p>
	 */
	private final void checkAndMarkComplete() {
		if(this.complete) {
			this.taskSession.updateStatus(Status.COMPLETE);
			GuiManager.getInstance().notice("下载完成", "任务下载完成：" + this.name());
		}
	}
	
	/**
	 * <p>垃圾回收</p>
	 */
	private final void gc() {
		LOGGER.debug("垃圾回收（GC）");
		System.gc();
	}
	
}
