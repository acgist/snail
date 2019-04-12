package com.acgist.snail.downloader.torrent;

import java.io.IOException;

import com.acgist.snail.downloader.Downloader;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentSessionManager;
import com.acgist.snail.utils.ThreadUtils;

public class TorrentDownloader extends Downloader {

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
		try {
			final String infoHashHex = MagnetProtocol.buildHash(entity.getUrl());
			torrentSession = TorrentSessionManager.getInstance().buildSession(infoHashHex, path);
			torrentSession.build(this.taskSession);
			taskSession.downloadSize(torrentSession.size());
		} catch (DownloadException e) {
			fail("获取Tracker失败");
		}
	}
	
}
