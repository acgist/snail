package com.acgist.snail.downloader.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.net.ftp.FtpManager;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.utils.FileUtils;

/**
 * FTP下载
 */
public class FtpDownloader extends AbstractDownloader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FtpDownloader.class);

	private FtpClient client;
	private byte[] bytes; // 速度byte
	private BufferedInputStream input; // 输入流
	private BufferedOutputStream output; // 输出流
	
	private FtpDownloader(TaskWrapper wrapper) {
		super(wrapper);
	}

	public static final FtpDownloader newInstance(TaskWrapper wrapper) {
		return new FtpDownloader(wrapper);
	}

	@Override
	public void open() {
		bytes = new byte[DownloadConfig.getBufferByte()];
		buildInput();
		buildOutput();
	}

	@Override
	public void download() throws IOException {
		int length = 0;
		long begin, end;
		final boolean ok = !complete; // 下载前没有标记完成
		while(ok) {
			if(!wrapper.download()) { // 已经不是下载状态
				break;
			}
			begin = System.currentTimeMillis();
			length = input.readNBytes(bytes, 0, bytes.length);
			if(isComplete(length)) { // 是否完成
				complete = true;
				break;
			}
			output.write(bytes, 0, length);
			statistical(length);
			end = System.currentTimeMillis();
			yield(end - begin);
		}
	}

	@Override
	public void release() {
		if(client != null) {
			client.close();
		}
		try {
			if(output != null) {
				output.flush(); // 刷新
				output.close();
			}
		} catch (IOException e) {
			LOGGER.error("关闭文件流失败", e);
		}
	}

	/**
	 * 任务是否完成：长度-1或者下载数据等于任务长度
	 */
	private boolean isComplete(int length) {
		long size = wrapper.entity().getSize();
		long downloadSize = wrapper.downloadSize();
		return length == -1 || size == downloadSize;
	}
	
	private void buildInput() {
		var entity = wrapper.entity();
		long size = FileUtils.fileSize(entity.getFile()); // 已下载大小
		client = FtpManager.buildClient(entity.getUrl());
		boolean ok = client.connect();
		if(ok) {
			InputStream inputStream = client.download(size);
			if(inputStream == null) {
				fail(client.failMessage());
			} else {
				this.input = new BufferedInputStream(inputStream);
				if(client.append()) {
					wrapper.downloadSize(size);
				} else {
					wrapper.downloadSize(0L);
				}
			}
		} else {
			fail("FTP服务器连接失败");
		}
	}
	
	private void buildOutput() {
		var entity = wrapper.entity();
		try {
			long size = wrapper.downloadSize();
			if(size == 0L) {
				output = new BufferedOutputStream(new FileOutputStream(entity.getFile()), DownloadConfig.getMemoryBufferByte());
			} else { // 支持续传
				output = new BufferedOutputStream(new FileOutputStream(entity.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("打开下载文件流失败", e);
			fail();
		}
	}
	
}
