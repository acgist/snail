package com.acgist.snail.downloader.torrent;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.Downloader;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentSessionManager;
import com.acgist.snail.utils.ThreadUtils;

public class TorrentDownloader extends Downloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentDownloader.class);
	
	private TorrentSession torrentSession;
	
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
			ThreadUtils.sleep(1000);
		}
	}

	@Override
	public void release() {
		torrentSession.release();
	}
	
	private void build() {
		var entity = this.taskSession.entity();
		final String path = entity.getTorrent();
		String infoHashHex = null;
		try {
			infoHashHex = MagnetProtocol.buildHash(entity.getUrl());
			torrentSession = TorrentSessionManager.getInstance().buildSession(infoHashHex, path);
		} catch (DownloadException e) {
			fail("获取种子信息失败");
			LOGGER.error("获取种子信息失败", e);
			return;
		}
		torrentSession.build(this.taskSession);
		try {
			torrentSession.loadTracker();
		} catch (DownloadException e) {
			fail("获取Tracker失败");
			LOGGER.error("获取Tracker异常", e);
			return;
		}
		taskSession.downloadSize(torrentSession.size());
	}
	
}
