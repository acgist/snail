package com.acgist.snail.downloader.torrent;

import java.io.IOException;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.pojo.session.TaskSession;

public class TorrentDownloader extends AbstractDownloader {

	private TorrentDownloader(TaskSession session) {
		super(session);
	}

	public static final TorrentDownloader newInstance(TaskSession session) {
		return new TorrentDownloader(session);
	}

	@Override
	public void open() {
		
	}

	@Override
	public void download() throws IOException {
		
	}

	@Override
	public void release() {
		
	}
	
}
