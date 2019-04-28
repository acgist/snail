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
	}

	public static final TorrentDownloader newInstance(TaskSession taskSession) {
		return new TorrentDownloader(taskSession);
	}

	@Override
	public void open() {
		build();
	}

	@Override
	public void download() throws IOException {
		while(ok()) {
			synchronized (downloadLock) {
				ThreadUtils.wait(downloadLock, Duration.ofSeconds(Integer.MAX_VALUE));
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
	
	private void build() {
		final var entity = this.taskSession.entity();
		final String path = entity.getTorrent();
		String infoHashHex = null;
		try {
			infoHashHex = MagnetProtocol.buildHash(entity.getUrl());
			torrentSession = TorrentSessionManager.getInstance().buildSession(infoHashHex, path);
		} catch (DownloadException e) {
			fail("获取种子信息失败");
			LOGGER.error("获取种子信息异常", e);
			return;
		}
		torrentSession.build(this.taskSession);
		try {
			torrentSession.loadTracker();
		} catch (DownloadException e) {
			fail("Tracker加载失败");
			LOGGER.error("Tracker加载异常", e);
			return;
		}
		taskSession.downloadSize(torrentSession.size());
	}
	
}
