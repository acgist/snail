package com.acgist.snail.downloader.torrent;

import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.Downloader;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentSessionManager;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>BT下载器</p>
 * TODO：下载完成向Tracker发送complete消息。
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentDownloader extends Downloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentDownloader.class);
	
	private TorrentSession torrentSession;
	
	private Object downloadLock = new Object(); // 下载锁
	
	private TorrentDownloader(TaskSession taskSession) {
		super(taskSession);
		load();
	}

	public static final TorrentDownloader newInstance(TaskSession taskSession) {
		return new TorrentDownloader(taskSession);
	}

	@Override
	public void open() {
		loadDownload();
	}

	@Override
	public void download() throws IOException {
		while(ok()) {
			synchronized (downloadLock) {
				ThreadUtils.wait(downloadLock, Duration.ofSeconds(Integer.MAX_VALUE));
				this.complete = torrentSession.torrentStreamGroup().complete();
			}
		}
	}

	@Override
	public void unlockDownload() {
		synchronized (downloadLock) {
			downloadLock.notifyAll();
		}
	}
	
	@Override
	public void release() {
		torrentSession.release();
	}

	/**
	 * 加载任务
	 */
	private void load() {
		final var entity = this.taskSession.entity();
		final String path = entity.getTorrent();
		try {
			final String infoHashHex = MagnetProtocol.buildHash(entity.getUrl());
			this.torrentSession = TorrentSessionManager.getInstance().buildSession(infoHashHex, path);
			this.torrentSession.loadTask(this.taskSession);
		} catch (DownloadException e) {
			fail("任务加载失败");
			LOGGER.error("任务加载异常", e);
			return;
		}
	}
	
	/**
	 * 加载下载
	 */
	private void loadDownload() {
		try {
			this.complete = this.torrentSession.download();
		} catch (DownloadException e) {
			fail("任务加载失败");
			LOGGER.error("任务加载异常", e);
			return;
		}
		taskSession.downloadSize(torrentSession.size());
	}

}
