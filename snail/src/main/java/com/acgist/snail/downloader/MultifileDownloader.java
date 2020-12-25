package com.acgist.snail.downloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>多文件任务下载器</p>
 * 
 * @author acgist
 */
public abstract class MultifileDownloader extends Downloader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MultifileDownloader.class);

	/**
	 * <p>下载锁</p>
	 * <p>下载时阻塞下载任务线程</p>
	 */
	protected final Object downloadLock = new Object();
	
	/**
	 * @param taskSession 任务信息
	 */
	protected MultifileDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	@Override
	public void open() throws NetException, DownloadException {
		// 创建文件目录：防止删除目录导致任务下载失败
		FileUtils.buildFolder(this.taskSession.getFile(), false);
		this.loadDownload();
	}

	@Override
	public void download() throws DownloadException {
		while(this.downloadable()) {
			// 添加下载锁
			synchronized (this.downloadLock) {
				try {
					this.downloadLock.wait(Long.MAX_VALUE);
				} catch (InterruptedException e) {
					LOGGER.debug("线程等待异常", e);
					Thread.currentThread().interrupt();
				}
				// 完成状态必须在同步块中检查
				this.complete = this.checkCompleted();
			}
		}
	}
	
	@Override
	public void unlockDownload() {
		synchronized (this.downloadLock) {
			this.downloadLock.notifyAll();
		}
	}
	
	/**
	 * <p>开始下载</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected abstract void loadDownload() throws DownloadException;

	/**
	 * <p>判断是否下载完成</p>
	 * 
	 * @return 是否下载完成
	 */
	protected abstract boolean checkCompleted();
	
}
