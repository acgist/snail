package com.acgist.snail.downloader;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.gui.GuiHandler.SnailNoticeType;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TaskSession.Status;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.IStatistics;
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
	
	/**
	 * 失败状态
	 */
	protected volatile boolean fail = false;
	/**
	 * 完成状态
	 */
	protected volatile boolean complete = false;
	/**
	 * 任务
	 */
	protected final TaskSession taskSession;
	/**
	 * 任务删除锁，删除后标记：true。
	 */
	private final AtomicBoolean deleteLock = new AtomicBoolean(false);

	protected Downloader(TaskSession taskSession) {
		this.taskSession = taskSession;
		this.taskSession.downloader(this);
		// 加载已下载大小
		this.taskSession.downloadSize(downloadSize());
		// 开始时不处于下载中时可以直接删除（标记删除锁）：暂停、完成等
		if(!this.taskSession.download()) {
			this.deleteLock.set(true);
		}
	}
	
	@Override
	public String id() {
		return this.taskSession.entity().getId();
	}
	
	@Override
	public boolean running() {
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
		GuiHandler.getInstance().notice("下载失败", noticeMessage.toString(), SnailNoticeType.warn);
	}
	
	@Override
	public void delete() {
		this.pause(); // 暂停
		if(!this.deleteLock.get()) {
			synchronized (this.deleteLock) {
				if(!this.deleteLock.get()) {
					ThreadUtils.wait(this.deleteLock, Duration.ofSeconds(5));
				}
			}
		}
		final TaskRepository repository = new TaskRepository();
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
			GuiHandler.getInstance().notice("下载完成", this.name() + "已经下载完成");
		}
	}
	
	@Override
	public long downloadSize() {
		return FileUtils.fileSize(this.taskSession.entity().getFile());
	}
	
	@Override
	public void run() {
		// 任务已经处于下载中直接跳过，防止多次点击暂停开始导致后面线程阻塞导致不能下载其他任务。
		if(this.taskSession.download()) {
			LOGGER.info("任务已经在下载中，停止执行：{}", this.name());
			return;
		}
		synchronized (this.taskSession) {
			var entity = this.taskSession.entity();
			if(this.taskSession.await()) {
				LOGGER.info("开始下载：{}", this.name());
				this.fail = false; // 标记下载失败
				this.deleteLock.set(false); // 设置删除锁
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
			this.deleteLock.set(true);
			this.deleteLock.notifyAll();
		}
	}
	
	/**
	 * 唤醒下载等待线程、更新任务状态
	 * 
	 * @param status 状态
	 */
	private void updateStatus(Status status) {
		this.unlockDownload();
		this.taskSession.updateStatus(status);
	}
	
}
