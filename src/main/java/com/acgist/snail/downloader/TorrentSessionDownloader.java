package com.acgist.snail.downloader;

import java.io.IOException;
import java.time.Duration;

import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>TorrentSession任务下载器</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public abstract class TorrentSessionDownloader extends Downloader {

	/**
	 * <p>Torrent任务信息</p>
	 */
	protected final TorrentSession torrentSession;
	/**
	 * <p>下载锁：下载时阻塞下载器线程，使用后台下载。</p>
	 */
	protected final Object downloadLock = new Object();
	
	protected TorrentSessionDownloader(TaskSession taskSession) {
		super(taskSession);
		this.torrentSession = this.loadTorrentSession();
	}
	
	@Override
	public void open() {
		loadDownload();
	}
	
	@Override
	public void download() throws IOException {
		while(ok()) {
			synchronized (this.downloadLock) {
				ThreadUtils.wait(this.downloadLock, Duration.ofSeconds(Integer.MAX_VALUE));
				this.complete = this.torrentSession.checkCompleted();
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
	 * <p>加载Torrent任务信息</p>
	 */
	protected abstract TorrentSession loadTorrentSession();

	/**
	 * <p>开始下载</p>
	 */
	protected abstract void loadDownload();
	
}
