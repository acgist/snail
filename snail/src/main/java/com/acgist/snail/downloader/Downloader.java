package com.acgist.snail.downloader;

import com.acgist.snail.Snail;
import com.acgist.snail.context.IStatisticsSession;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.utils.StringUtils;

/**
 * 下载器适配器
 * 
 * @author acgist
 */
public abstract class Downloader implements IDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);
	
	/**
	 * 任务失败状态
	 */
	protected volatile boolean fail;
	/**
	 * 任务完成状态
	 */
	protected volatile boolean completed;
	/**
	 * 任务信息
	 */
	protected final ITaskSession taskSession;
	/**
	 * 统计信息
	 */
	protected final IStatisticsSession statistics;
	
	/**
	 * @param taskSession 任务信息
	 */
	protected Downloader(ITaskSession taskSession) {
		// 重置任务下载大小
		taskSession.buildDownloadSize();
		// 初始配置
		this.fail = false;
		this.completed = false;
		this.taskSession = taskSession;
		this.statistics = taskSession.getStatistics();
	}
	
	/**
	 * 验证文件是否完成
	 * 
	 * @param length 下载数据大小
	 * @param downloadSize 累计下载大小
	 * @param fileSize 文件大小
	 * 
	 * @return 是否完成
	 */
	public static final boolean checkFinish(int length, long downloadSize, long fileSize) {
		return
			// 没有更多数据
			length < 0 ||
			// 累计下载大小大于文件大小
			// 需要验证文件大小：可能存在不能正常获取网络文件大小
			(0L < fileSize && fileSize <= downloadSize);
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
	public final boolean statusDownload() {
		return this.taskSession.statusDownload();
	}
	
	@Override
	public final boolean statusPause() {
		return this.taskSession.statusPause();
	}
	
	@Override
	public final boolean statusCompleted() {
		return this.taskSession.statusCompleted();
	}
	
	@Override
	public final boolean statusFail() {
		return this.taskSession.statusFail();
	}
	
	@Override
	public final boolean statusDelete() {
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
		final boolean verify = this.taskSession.getDownloadFile().exists();
		if(!verify) {
			// 如果文件已被删除重置已经下载大小
			this.taskSession.setDownloadSize(0L);
		}
		return verify;
	}
	
	@Override
	public final void fail(String message) {
		// 修改任务状态
		this.taskSession.updateStatus(Status.FAIL);
		// 设置失败信息
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
		GuiContext.getInstance().notice("下载失败", noticeMessage.toString(), GuiContext.MessageType.WARN);
	}
	
	@Override
	public void unlockDownload() {
	}
	
	@Override
	public void release() {
		// GC释放内存
		SystemContext.gc();
		// 注意：任务释放完成解锁（防止提前退出程序导致数据没有保存）
		Snail.getInstance().unlockDownload();
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
					// 重置状态
					this.fail = false;
					this.completed = false;
					this.taskSession.setStatus(Status.DOWNLOAD);
					try {
						this.open();
						this.download();
					} catch (Exception e) {
						LOGGER.error("任务下载异常", e);
						this.fail(e.getMessage());
					}
					this.checkCompletedAndMark();
					this.release();
					// 解除删除锁
					this.taskSession.unlockDelete();
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
	 * 判断任务是否可以下载
	 * 1.未被标记{@linkplain #fail 失败}
	 * 2.未被标记{@linkplain #completed 完成}
	 * 3.任务处于{@linkplain #statusDownload() 下载状态}
	 * 
	 * @return 是否可以下载
	 */
	protected final boolean downloadable() {
		return
			!this.fail &&
			!this.completed &&
			this.statusDownload();
	}
	
	/**
	 * 检测任务是否完成并且标记
	 */
	private final void checkCompletedAndMark() {
		if(this.completed) {
			this.taskSession.updateStatus(Status.COMPLETED);
			GuiContext.getInstance().notice("下载完成", "任务下载完成：" + this.name());
		}
	}
	
}
