package com.acgist.snail.downloader;

import java.time.Duration;

import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>多文件任务下载器</p>
 * 
 * @author acgist
 * @since 1.4.1
 */
public abstract class MultiFileDownloader extends Downloader {

	/**
	 * <p>下载锁</p>
	 * <p>下载时阻塞下载任务线程</p>
	 */
	protected final Object downloadLock = new Object();
	
	protected MultiFileDownloader(ITaskSession taskSession) {
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
	
}
