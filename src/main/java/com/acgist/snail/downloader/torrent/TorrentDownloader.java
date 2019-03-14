package com.acgist.snail.downloader.torrent;

import java.io.IOException;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

public class TorrentDownloader extends AbstractDownloader {

	private TrackerManager manager;
	
	private TorrentDownloader(TaskWrapper wrapper) {
		super(wrapper);
	}

	public static final TorrentDownloader newInstance(TaskWrapper wrapper) {
		return new TorrentDownloader(wrapper);
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
