package com.acgist.snail.downloader.ed2k;

import java.io.IOException;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

/**
 * ED2K下载器
 */
public class Ed2kDownloader extends AbstractDownloader {

	private Ed2kDownloader(TaskWrapper wrapper) {
		super(wrapper);
	}

	public static final Ed2kDownloader newInstance(TaskWrapper wrapper) {
		return new Ed2kDownloader(wrapper);
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
