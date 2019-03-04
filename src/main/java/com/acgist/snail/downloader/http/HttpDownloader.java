package com.acgist.snail.downloader.http;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

/**
 * 端点续传：https://www.cnblogs.com/findumars/p/5745345.html
 * 请求数据：
 * 		range: bytes=begin-end
 * 		content-range: bytes begin-end/total
 * 测试：http://my.163.com/zmb/
 */
public class HttpDownloader extends AbstractDownloader implements IDownloader {

	public HttpDownloader(TaskWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void run() {
	}

	@Override
	public void refresh() {
	}

}
