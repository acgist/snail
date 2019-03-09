package com.acgist.snail.downloader.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

/**
 * FTP下载
 */
public class FtpDownloader extends AbstractDownloader {

	private FTPClient client;
	
	public FtpDownloader(TaskWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void open() {
		client = new FTPClient();
	}

	@Override
	public void download() throws IOException {
	}

	@Override
	public void release() {
	}

}
