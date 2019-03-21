package com.acgist.snail.downloader.torrent;

import java.io.IOException;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
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
		this.session.entity().getUrl();
		torrentSession = TorrentSessionManager.getInstance().buildSession(path);
	}

	@Override
	public void download() throws IOException {
	}

	@Override
	public void release() {
	}
	
}
