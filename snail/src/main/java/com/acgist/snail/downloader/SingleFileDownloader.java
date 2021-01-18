package com.acgist.snail.downloader;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.StreamContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.StreamSession;

/**
 * <p>单文件任务下载器</p>
 * 
 * TODO：分段下载技术（断点续传支持：突破网盘限速）
 * 
 * @author acgist
 */
public abstract class SingleFileDownloader extends Downloader {
	
	/**
	 * <p>输入流</p>
	 */
	protected ReadableByteChannel input;
	/**
	 * <p>输出流</p>
	 */
	protected WritableByteChannel output;
	/**
	 * <p>数据流信息</p>
	 */
	private StreamSession streamSession;
	
	/**
	 * @param taskSession 下载任务
	 */
	protected SingleFileDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>创建{@linkplain #output 输出流}时需要验证服务端是否支持断点续传，所以优先创建{@linkplain #input 输入流}获取服务端信息。</p>
	 */
	@Override
	public void open() throws NetException, DownloadException {
		this.buildInput();
		this.buildOutput();
	}

	@Override
	public void download() throws DownloadException {
		int length = 0;
		final long fileSize = this.taskSession.getSize();
		this.streamSession = StreamContext.getInstance().newStreamSession(this.input);
		final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.DEFAULT_EXCHANGE_BYTES_LENGTH);
		try {
			while(this.downloadable()) {
				this.input.read(buffer);
				length = buffer.limit();
				if(length >= 0) {
					buffer.flip();
					this.output.write(buffer);
					buffer.clear();
					this.statistics.download(length);
					this.statistics.downloadLimit(length);
				}
				this.streamSession.heartbeat();
				if(StreamContext.checkFinish(length, this.taskSession.downloadSize(), fileSize)) {
					this.completed = true;
					break;
				}
			}
		} catch (Exception e) {
			throw new DownloadException("数据流操作失败", e);
		} finally {
			this.streamSession.remove();
		}
	}

	@Override
	public void unlockDownload() {
		super.unlockDownload();
		if(this.streamSession != null) {
			// 快速失败
			this.streamSession.fastCheckLive();
		}
	}
	
	/**
	 * <p>创建{@linkplain #output 输出流}</p>
	 * <p>通过判断任务已下载大小判断是否支持断点续传</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildOutput() throws DownloadException {
		try {
			final long size = this.taskSession.downloadSize();
			BufferedOutputStream outputStream;
			if(size == 0L) {
				// 不支持断点续传
				outputStream = new BufferedOutputStream(new FileOutputStream(this.taskSession.getFile()), DownloadConfig.getMemoryBufferByte());
			} else {
				// 支持断点续传
				outputStream = new BufferedOutputStream(new FileOutputStream(this.taskSession.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
			this.output = Channels.newChannel(outputStream);
		} catch (FileNotFoundException e) {
			throw new DownloadException("下载文件打开失败", e);
		}
	}
	
	/**
	 * <p>创建{@linkplain #input 输入流}</p>
	 * <p>验证是否支持断点续传，如果支持重新设置任务已下载大小。</p>
	 * 
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	protected abstract void buildInput() throws NetException, DownloadException;

}
