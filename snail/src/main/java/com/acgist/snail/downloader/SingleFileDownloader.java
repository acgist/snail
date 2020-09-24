package com.acgist.snail.downloader;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.StreamContext;
import com.acgist.snail.context.StreamContext.StreamSession;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.ITaskSession;

/**
 * <p>单文件任务下载器</p>
 * 
 * TODO：分段下载技术（断点续传支持：突破网盘限速）
 * TODO：大文件下载内存优化
 * 
 * @author acgist
 * @since 1.1.1
 */
public abstract class SingleFileDownloader extends Downloader {

//	private static final Logger LOGGER = LoggerFactory.getLogger(SingleFileDownloader.class);
	
	/**
	 * <p>输入流</p>
	 */
	protected InputStream input;
	/**
	 * <p>输出流</p>
	 */
	protected OutputStream output;
	/**
	 * <p>数据流信息</p>
	 */
	private StreamSession streamSession;
	
	protected SingleFileDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>创建{@linkplain #output 输出流}时需要验证是否支持断点续传，所以优先创建{@linkplain #input 输入流}。</p>
	 */
	@Override
	public void open() throws NetException, DownloadException {
		this.buildInput();
		this.buildOutput();
	}

	@Override
	public void download() throws DownloadException {
		int length = 0;
		final byte[] bytes = new byte[SystemConfig.DEFAULT_EXCHANGE_BYTES_LENGTH];
		this.streamSession = StreamContext.getInstance().newStreamSession(this.input);
		try {
			while(this.downloadable()) {
				// TODO：如果不能读取数据会阻塞任务暂停，可以将streamSession提到类变量，然后在unlockDownload方法中强制关闭。
				length = this.input.read(bytes, 0, bytes.length);
				if(this.isComplete(length)) {
					this.complete = true;
					break;
				}
				this.output.write(bytes, 0, length);
				this.streamSession.heartbeat();
				this.download(length);
			}
		} catch (Exception e) {
			throw new DownloadException("数据流操作失败", e);
		} finally {
			StreamContext.getInstance().removeStreamSession(this.streamSession);
		}
	}

	@Override
	public void unlockDownload() {
		if(this.streamSession != null) {
			this.streamSession.fastCheckLive();
		}
	}
	
	/**
	 * <dl>
	 * 	<dt>判断任务是否下载完成</dt>
	 * 	<dd>读取数据长度小于等于{@code -1}</dd>
	 * 	<dd>任务长度小于等于已下载数据长度</dd>
	 * </dl>
	 * 
	 * @param length 读取数据长度
	 * 
	 * @return {@code true}-下载完成；{@code false}-没有完成；
	 */
	protected boolean isComplete(int length) {
		return
			length <= -1 ||
			this.taskSession.getSize() <= this.taskSession.downloadSize();
	}
	
	/**
	 * <p>创建{@linkplain #output 输出流}</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected void buildOutput() throws DownloadException {
		try {
			final long size = this.taskSession.downloadSize();
			if(size == 0L) { // 不支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(this.taskSession.getFile()), DownloadConfig.getMemoryBufferByte());
			} else { // 支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(this.taskSession.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			throw new DownloadException("下载文件打开失败", e);
		}
	}
	
	/**
	 * <p>创建{@linkplain #input 输入流}</p>
	 * <p>先验证是否支持断点续传，如果支持重新设置任务已下载大小。</p>
	 * 
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	protected abstract void buildInput() throws NetException, DownloadException;

}
