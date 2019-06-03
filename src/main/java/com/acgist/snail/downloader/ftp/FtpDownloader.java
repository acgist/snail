package com.acgist.snail.downloader.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.Downloader;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.net.ftp.bootstrap.FtpClientFactory;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>FTP下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FtpDownloader extends Downloader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FtpDownloader.class);

	private byte[] bytes; // 速度byte
	private FtpClient client; // FtpClient
	private BufferedInputStream input; // 输入流
	private BufferedOutputStream output; // 输出流
	
	private FtpDownloader(TaskSession taskSession) {
		super(taskSession);
	}

	public static final FtpDownloader newInstance(TaskSession taskSession) {
		return new FtpDownloader(taskSession);
	}

	@Override
	public void open() {
		this.bytes = new byte[128 * 1024];
		buildInput();
		buildOutput();
	}

	@Override
	public void download() throws IOException {
		LOGGER.debug("FTP任务开始下载");
		int length = 0;
		while(ok()) {
			length = this.input.read(this.bytes, 0, this.bytes.length);
			if(isComplete(length)) { // 是否完成
				this.complete = true;
				break;
			}
			this.output.write(this.bytes, 0, length);
			this.download(length);
		}
	}

	@Override
	public void release() {
		if(this.client != null) {
			this.client.close();
		}
		try {
			if(this.output != null) {
				this.output.flush(); // 刷新
				this.output.close();
			}
		} catch (IOException e) {
			LOGGER.error("关闭FTP文件流异常", e);
		}
	}

	/**
	 * 任务是否完成：长度-1或者下载数据等于任务长度
	 */
	private boolean isComplete(int length) {
		final long size = this.taskSession.entity().getSize();
		final long downloadSize = this.taskSession.downloadSize();
		return length == -1 || size == downloadSize;
	}
	
	/**
	 * 创建FTP输入流、设置已下载文件大小
	 */
	private void buildInput() {
		var entity = this.taskSession.entity();
		long size = FileUtils.fileSize(entity.getFile()); // 已下载大小
		this.client = FtpClientFactory.build(entity.getUrl());
		boolean ok = this.client.connect();
		if(ok) {
			final InputStream inputStream = this.client.download(size);
			if(inputStream == null) {
				fail(this.client.failMessage());
			} else {
				this.input = new BufferedInputStream(inputStream);
				if(this.client.append()) {
					this.taskSession.downloadSize(size);
				} else {
					this.taskSession.downloadSize(0L);
				}
			}
		} else {
			fail("FTP服务器连接失败");
		}
	}
	
	/**
	 * 创建输出流
	 */
	private void buildOutput() {
		var entity = this.taskSession.entity();
		try {
			long size = this.taskSession.downloadSize();
			if(size == 0L) {
				this.output = new BufferedOutputStream(new FileOutputStream(entity.getFile()), DownloadConfig.getMemoryBufferByte());
			} else { // 支持续传
				this.output = new BufferedOutputStream(new FileOutputStream(entity.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			fail("文件打开失败");
			LOGGER.error("打开FTP文件流失败", e);
		}
	}
	
}
