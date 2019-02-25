package com.acgist.snail.downloader.http;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

public class HttpDownloader extends AbstractDownloader implements IDownloader {

	public HttpDownloader(TaskWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void run() {
		
	}

}
