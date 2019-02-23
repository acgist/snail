package com.acgist.snail.downloader.http;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

public class HttpDownloader extends AbstractDownloader implements IDownloader {

	public HttpDownloader(TaskWrapper task) {
		super(task);
	}

	@Override
	public void run() {
		
	}

	@Override
	public String name() {
		return null;
	}

	@Override
	public void start() {
		
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void delete() {
		
	}

}
