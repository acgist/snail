package com.acgist.snail.downloader.torrent;

import java.io.IOException;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentSessionManager;

public class TorrentDownloader extends AbstractDownloader {

	private TorrentSession torrentSession;
	
	private TorrentDownloader(TaskSession session) {
		super(session);
	}

	public static final TorrentDownloader newInstance(TaskSession session) {
		return new TorrentDownloader(session);
	}

	@Override
	public void open() {
		buildTracker();
	}

	@Override
	public void download() throws IOException {
	}

	@Override
	public void release() {
	}
	
	private void buildTracker() {
		var entity = this.session.entity();
		String path = entity.getTorrent();
		try {
			String hashHex = MagnetProtocol.buildHash(entity.getUrl());
			torrentSession = TorrentSessionManager.getInstance().buildSession(hashHex, path);
			torrentSession.loadTracker(this.session);
		} catch (DownloadException e) {
			fail("获取Tracker失败");
		}
	}
	
}
