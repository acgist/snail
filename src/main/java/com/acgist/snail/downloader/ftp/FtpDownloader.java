package com.acgist.snail.downloader.ftp;

import java.io.IOException;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

/**
 * FTP下载
 */
public class FtpDownloader extends AbstractDownloader {

	public FtpDownloader(TaskWrapper wrapper) {
		super(wrapper);
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
