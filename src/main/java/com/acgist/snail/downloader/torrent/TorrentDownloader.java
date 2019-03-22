package com.acgist.snail.downloader.torrent;

import java.io.IOException;

import com.acgist.snail.downloader.Downloader;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentSessionManager;

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
	}

	@Override
	public void release() {
		torrentSession.release();
	}
	
	private void build() {
		var entity = this.taskSession.entity();
		String path = entity.getTorrent();
		try {
			String hashHex = MagnetProtocol.buildHash(entity.getUrl());
			torrentSession = TorrentSessionManager.getInstance().buildSession(hashHex, path);
			torrentSession.build(this.taskSession);
		} catch (DownloadException e) {
			fail("获取Tracker失败");
		}
	}
	
}
