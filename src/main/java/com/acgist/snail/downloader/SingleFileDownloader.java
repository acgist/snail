package com.acgist.snail.downloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.system.config.DownloadConfig;

/**
 * <p>单个文件下载器抽象类</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public abstract class SingleFileDownloader extends Downloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(SingleFileDownloader.class);
	
	/**
	 * 缓存字节数组
	 */
	protected final byte[] bytes;
	/**
	 * 输入流
	 */
	protected BufferedInputStream input;
	/**
	 * 输出流
	 */
	protected BufferedOutputStream output;
	
	protected SingleFileDownloader(byte[] bytes, TaskSession taskSession) {
		super(taskSession);
		this.bytes = bytes;
	}
	
	/**
	 * 创建下载输出流
	 */
	protected void buildOutput() {
		final var entity = this.taskSession.entity();
		try {
			final long size = this.taskSession.downloadSize();
			if(size == 0L) {
				this.output = new BufferedOutputStream(new FileOutputStream(entity.getFile()), DownloadConfig.getMemoryBufferByte());
			} else {
				this.output = new BufferedOutputStream(new FileOutputStream(entity.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			fail("下载文件打开失败");
			LOGGER.error("打开下载文件流异常", e);
		}
	}

}
