package com.acgist.snail.downloader;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.gui.GuiHandler.SnailNoticeType;
import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskSession.Status;
import com.acgist.snail.system.IStatistics;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Downloader implements IDownloader, IStatistics {

	private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);
	
	/**
	 * <p>{@linkplain #deleteLock 删除锁}等待时间（秒）：{@value}</p>
	 */
	private static final int DELETE_WAIT_TIME = 5;
	
	/**
	 * <p>任务失败状态</p>
	 * <p>{@code true}-失败；{@code false}-正常；</p>
	 */
	protected volatile boolean fail = false;
	/**
	 * <p>任务完成状态</p>
	 * <p>{@code true}-下载完成；{@code false}-没有完成；</p>
	 */
	protected volatile boolean complete = false;
	/**
	 * <p>任务信息</p>
	 */
	protected final ITaskSession taskSession;
	/**
	 * <p>删除锁</p>
	 * <p>{@code true}-任务没有下载（可以删除）；</p>
	 * <p>{@code false}-任务正在下载（不能删除）；</p>
	 * <p>任务删除时检查该锁，判断是否可以删除任务，如果任务不能删除需要等待任务结束。</p>
	 */
	private final AtomicBoolean deleteLock = new AtomicBoolean(false);

	protected Downloader(ITaskSession taskSession) {
		this.taskSession = taskSession;
		// 已下载大小
		this.taskSession.downloadSize(downloadSize());
		// 任务没有下载标记删除锁：true
		if(!this.taskSession.download()) {
			this.deleteLock.set(true);
		}
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
	public boolean downloading() {
		return this.taskSession.download();
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
		GuiHandler.getInstance().notice("下载失败", noticeMessage.toString(), SnailNoticeType.WARN);
	}
	
	@Override
	public void delete() {
		this.pause(); // 暂停任务
		// 等待删除锁释放
		if(!this.deleteLock.get()) {
			synchronized (this.deleteLock) {
				if(!this.deleteLock.get()) {
					ThreadUtils.wait(this.deleteLock, Duration.ofSeconds(DELETE_WAIT_TIME));
				}
			}
		}
		this.taskSession.delete(); // 删除任务
	}
	
	@Override
	public void refresh() {
	}
	
	@Override
	public void unlockDownload() {
	}
	
	@Override
	public void checkComplete() {
		if(this.complete) {
			this.updateStatus(Status.COMPLETE);
			GuiHandler.getInstance().notice("下载完成", "任务下载完成：" + this.name());
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>默认直接获取本地文件大小，这种方式可能会出现误差，必要时请重写并通过{@link ITaskSession#downloadSize(long)}方法设置已下载大小。</p>
	 */
	@Override
	public long downloadSize() {
		return FileUtils.fileSize(this.taskSession.getFile());
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
					fail(e.getMessage());
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
	
	@Override
	public void upload(int buffer) {
		this.taskSession.statistics().upload(buffer);
	}
	
	@Override
	public void download(int buffer) {
		this.taskSession.statistics().download(buffer);
	}
	
	@Override
	public IStatisticsSession statistics() {
		return this.taskSession.statistics();
	}
	
	/**
	 * <dl>
	 * 	<dt>判断任务是否可以下载</dt>
	 * 	<dd>未被标记{@linkplain #fail 失败}</dd>
	 * 	<dd>未被标记{@linkplain #complete 完成}</dd>
	 * 	<dd>任务处于{@linkplain ITaskSession#download() 下载状态}</dd>
	 * </dl>
	 * 
	 * @return {@code true}-可以下载；{@code false}-不能下载；
	 */
	protected boolean downloadable() {
		return
			!this.fail &&
			!this.complete &&
			this.taskSession.download();
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
