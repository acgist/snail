package com.acgist.snail.downloader;

import java.time.Duration;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>多文件任务下载器</p>
 * 
 * @author acgist
 * @since 1.4.1
 */
public abstract class MultifileDownloader extends Downloader {

	/**
	 * <p>下载锁</p>
	 * <p>下载时阻塞下载任务线程</p>
	 */
	protected final Object downloadLock = new Object();
	
	protected MultifileDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	@Override
	public void open() throws NetException, DownloadException {
		this.loadDownload();
	}

	@Override
	public void download() throws DownloadException {
		while(this.downloadable()) {
			synchronized (this.downloadLock) {
				ThreadUtils.wait(this.downloadLock, Duration.ofSeconds(Integer.MAX_VALUE));
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
	 * @return true-完成；false-没有完成；
	 */
	protected abstract boolean checkCompleted();
	
}
