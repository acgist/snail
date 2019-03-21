package com.acgist.snail.downloader.ed2k;

import java.io.IOException;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.pojo.session.TaskSession;

/**
 * ED2K下载器
 */
public class Ed2kDownloader extends AbstractDownloader {

	private Ed2kDownloader(TaskSession taskSession) {
		super(taskSession);
	}

	public static final Ed2kDownloader newInstance(TaskSession taskSession) {
		return new Ed2kDownloader(taskSession);
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
