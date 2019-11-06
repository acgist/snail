package com.acgist.snail.downloader.torrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.TorrentSessionDownloader;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>BT下载器</p>
 * <p>任务{@link #open()}时，打开分享功能，直到任务被删除或者软件重启。</p>
 * <p>重启后如果继续下载，依旧会开启分享功能。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentDownloader extends TorrentSessionDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentDownloader.class);
	
	private TorrentDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	public static final TorrentDownloader newInstance(ITaskSession taskSession) {
		return new TorrentDownloader(taskSession);
	}
	
	@Override
	public void delete() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseUpload(); // 释放BT资源
		}
		super.delete();
	}
	
	@Override
	public void refresh() {
		// TODO：添加下载文件TorrentStreamGroup
	}
	
	@Override
	public void release() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseDownload();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>加载完成立即使任务可以被分享</p>
	 */
	@Override
	protected TorrentSession loadTorrentSession() {
		final var torrentSession = super.loadTorrentSession();
		if(torrentSession != null) {
			try {
				torrentSession.upload(this.taskSession);
			} catch (DownloadException e) {
				LOGGER.error("BT任务分享异常", e);
				fail("BT任务分享失败：" + e.getMessage());
			}
		}
		return torrentSession;
	}
	
	@Override
	protected void loadDownload() {
		try {
			if(this.torrentSession != null) {
				this.complete = this.torrentSession.download();
			}
		} catch (DownloadException e) {
			LOGGER.error("BT任务加载异常", e);
			fail("BT任务加载失败：" + e.getMessage());
		}
	}

}
