package com.acgist.snail.downloader;

import java.awt.TrayIcon.MessageType;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.menu.TrayMenu;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.statistics.IStatistics;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>下载器抽象类</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Downloader implements IDownloader, IStatistics {

	private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);
	
	protected volatile boolean fail = false; // 失败状态
	protected volatile boolean complete = false; // 下载完成
	
	private final Object deleteLock = new Object();
	
	protected final TaskSession taskSession;

	protected Downloader(TaskSession taskSession) {
		this.taskSession = taskSession;
		this.taskSession.downloader(this);
		this.taskSession.downloadSize(downloadSize()); // 加载已下载大小
	}
	
	@Override
	public String id() {
		return this.taskSession.entity().getId();
	}
	
	@Override
	public boolean downloading() {
		return this.taskSession.download();
	}
	
	@Override
	public String name() {
		return this.taskSession.entity().getName();
	}
	
	@Override
	public TaskSession taskSession() {
		return this.taskSession;
	}
	
	@Override
	public void start() {
		if(this.taskSession.download()) {
			return;
		}
		this.updateStatus(Status.await);
	}
	
	@Override
	public void pause() {
		if(this.taskSession.pause()) {
			return;
		}
		this.updateStatus(Status.pause);
	}
	
	@Override
	public void fail(String message) {
		this.fail = true;
		this.updateStatus(Status.fail);
		final StringBuilder noticeMessage = new StringBuilder();
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
		synchronized (this.deleteLock) {
			ThreadUtils.wait(this.deleteLock, Duration.ofSeconds(5));
		}
		TaskRepository repository = new TaskRepository();
		repository.delete(this.taskSession.entity());
	}
	
	@Override
	public void refresh() {
	}
	
	@Override
	public void unlockDownload() {
	}
	
	@Override
	public void complete() {
		if(this.complete) {
			this.updateStatus(Status.complete);
			TrayMenu.getInstance().notice("下载完成", name() + "已经下载完成");
		}
	}
	
	@Override
	public long downloadSize() {
		return FileUtils.fileSize(this.taskSession.entity().getFile());
	}
	
	@Override
	public void run() {
		if(this.taskSession.download()) { // 任务已经处于下载中直接跳过，防止多次点击暂停开始导致后面线程阻塞导致不能下载其他任务
			LOGGER.info("任务已经在下载中，停止执行：{}", name());
			return;
		}
		synchronized (this.taskSession) {
			var entity = this.taskSession.entity();
			if(this.taskSession.await()) {
				LOGGER.info("开始下载：{}", name());
				this.fail = false; // 标记下载失败
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
				this.unlockDelete(); // 解除删除锁
				LOGGER.info("下载结束：{}", name());
			}
		}
	}
	
	@Override
	public void download(long buffer) {
		this.taskSession.statistics().download(buffer);
	}

	@Override
	public void upload(long buffer) {
		this.taskSession.statistics().upload(buffer);
	}
	
	/**
	 * <p>判断是否可以下载：</p>
	 * <ul>
	 * 	<li>未被标记失败</li>
	 * 	<li>未被标记完成</li>
	 * 	<li>任务状态处于下载中</li>
	 * </ul>
	 */
	protected boolean ok() {
		return !this.fail && !this.complete && this.taskSession.download();
	}

	/**
	 * <p>唤醒删除</p>
	 * <p>如果任务被删除，需要等待下载正常结束。</p>
	 */
	private void unlockDelete() {
		synchronized (this.deleteLock) {
			this.deleteLock.notifyAll();
		}
	}
	
	/**
	 * 唤醒下载等待线程、更新状态
	 * @param status 状态
	 */
	private void updateStatus(Status status) {
		this.unlockDownload();
		this.taskSession.updateStatus(status);
	}
	
}
