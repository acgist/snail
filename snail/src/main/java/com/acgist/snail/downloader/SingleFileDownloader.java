package com.acgist.snail.downloader;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>单文件任务下载器</p>
 * 
 * @author acgist
 */
public abstract class SingleFileDownloader extends Downloader {
	
	/**
	 * <p>快速失败时间（毫秒）：{@value}</p>
	 * <p>注意：建议不要超过任务删除等待时间</p>
	 */
	private static final long FAST_CHECK_TIME = 2L * SystemConfig.ONE_SECOND_MILLIS;
	
	/**
	 * <p>输入流</p>
	 */
	protected ReadableByteChannel input;
	/**
	 * <p>输出流</p>
	 */
	protected WritableByteChannel output;
	/**
	 * <p>快速失败检测时间</p>
	 */
	private volatile long fastCheckTime;
	
	/**
	 * @param taskSession 下载任务
	 */
	protected SingleFileDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	@Override
	public void open() throws NetException, DownloadException {
		this.buildInput();
		this.buildOutput();
	}

	@Override
	public void download() throws DownloadException {
		int length = 0;
		final long fileSize = this.taskSession.getSize();
		final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.DEFAULT_EXCHANGE_LENGTH);
		try {
			while(this.downloadable()) {
				length = this.input.read(buffer);
				if(length >= 0) {
					buffer.flip();
					this.output.write(buffer);
					buffer.compact();
					this.statistics.download(length);
					this.statistics.downloadLimit(length);
					this.fastCheckTime = System.currentTimeMillis();
				}
				if(Downloader.checkFinish(length, this.taskSession.downloadSize(), fileSize)) {
					this.completed = true;
					break;
				}
			}
		} catch (IOException e) {
			// 防止偶然下载失败：通过验证下载时间、下载数据大小进行重试下载
			throw new DownloadException("数据流操作失败", e);
		}
	}
	
	@Override
	public void unlockDownload() {
		super.unlockDownload();
		// 快速失败检测
		if(System.currentTimeMillis() - this.fastCheckTime > FAST_CHECK_TIME) {
			IoUtils.close(this.input);
		}
	}

	/**
	 * <p>新建{@linkplain #output 输出流}</p>
	 * <p>通过判断任务已经下载大小验证是否支持断点续传</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildOutput() throws DownloadException {
		try {
			final long size = this.taskSession.downloadSize();
			final int bufferSize = DownloadConfig.getMemoryBufferByte(this.taskSession.getSize());
			OutputStream outputStream;
			if(size > 0L) {
				// 支持断点续传
				outputStream = new FileOutputStream(this.taskSession.getFile(), true);
			} else {
				// 不支持断点续传
				outputStream = new FileOutputStream(this.taskSession.getFile());
			}
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, bufferSize);
			this.output = Channels.newChannel(bufferedOutputStream);
		} catch (FileNotFoundException e) {
			throw new DownloadException("下载文件打开失败", e);
		}
	}
	
	/**
	 * <p>新建{@linkplain #input 输入流}</p>
	 * <p>验证是否支持断点续传：如果支持重新设置任务已经下载大小</p>
	 * 
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	protected abstract void buildInput() throws NetException, DownloadException;

}
