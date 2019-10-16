package com.acgist.snail.downloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.system.config.DownloadConfig;

/**
 * <p>单个文件任务下载器</p>
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
	
	@Override
	public void open() {
		buildInput();
		buildOutput();
	}

	@Override
	public void download() throws IOException {
		int length = 0;
		while(ok()) {
			// TODO：阻塞线程，导致暂停不能正常结束。
			length = this.input.read(this.bytes, 0, this.bytes.length);
			if(isComplete(length)) {
				this.complete = true;
				break;
			}
			this.output.write(this.bytes, 0, length);
			this.download(length);
		}
	}
	
	/**
	 * <p>判断任务是否完成：读取长度等于-1或者下载数据等于任务长度。</p>
	 */
	protected boolean isComplete(int length) {
		final long size = this.taskSession.entity().getSize();
		final long downloadSize = this.taskSession.downloadSize();
		return length == -1 || size == downloadSize;
	}
	
	/**
	 * <p>创建下载输出流</p>
	 */
	protected void buildOutput() {
		final var entity = this.taskSession.entity();
		try {
			final long size = this.taskSession.downloadSize();
			if(size == 0L) { // 文件大小=0：不支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(entity.getFile()), DownloadConfig.getMemoryBufferByte());
			} else { // 支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(entity.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("下载文件打开异常", e);
			fail("下载文件打开失败：" + e.getMessage());
		}
	}
	
	/**
	 * <p>创建下载输入流</p>
	 * <p>需要验证是否支持断点续传，如果支持需要重新设置任务已下载大小。</p>
	 */
	protected abstract void buildInput();

}
