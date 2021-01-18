package com.acgist.snail.net.hls;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.StreamContext;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.HlsSession;
import com.acgist.snail.pojo.session.StreamSession;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>HLS客户端</p>
 * 
 * @author acgist
 */
public final class HlsClient implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(HlsClient.class);
	
	/**
	 * <p>下载路径</p>
	 */
	private final String link;
	/**
	 * <p>下载文件路径</p>
	 */
	private final String path;
	/**
	 * <p>文件大小</p>
	 * <p>通过HTTP HEAD请求获取</p>
	 */
	private long size;
	/**
	 * <p>是否支持断点续传</p>
	 */
	private boolean range;
	/**
	 * <p>是否下载完成</p>
	 */
	private volatile boolean completed;
	/**
	 * <p>HLS任务信息</p>
	 */
	private final HlsSession hlsSession;
	/**
	 * <p>输入流</p>
	 */
	protected ReadableByteChannel input;
	/**
	 * <p>输出流</p>
	 */
	protected WritableByteChannel output;
	
	/**
	 * @param link 下载路径
	 * @param taskSession 任务信息
	 * @param hlsSession HLS任务信息
	 */
	public HlsClient(String link, ITaskSession taskSession, HlsSession hlsSession) {
		this.link = link;
		final String fileName = FileUtils.fileName(link);
		this.path = FileUtils.file(taskSession.getFile(), fileName);
		this.range = false;
		this.completed = false;
		this.hlsSession = hlsSession;
	}

	@Override
	public void run() {
		if(this.completed) {
			LOGGER.debug("HLS任务已经完成：{}", this.link);
			return;
		}
		if(!this.hlsSession.downloadable()) {
			LOGGER.debug("HLS任务不能下载：{}", this.link);
			return;
		}
		LOGGER.debug("下载文件：{}", this.link);
		// 已下载大小
		long downloadSize = FileUtils.fileSize(this.path);
		this.completed = this.checkCompleted(downloadSize);
		if(this.completed) {
			LOGGER.debug("HLS文件校验成功：{}", this.link);
		} else {
			int length = 0;
			final StreamSession streamSession = StreamContext.getInstance().newStreamSession(this.input);
			final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.DEFAULT_EXCHANGE_BYTES_LENGTH);
			try {
				this.buildInput(downloadSize);
				this.buildOutput();
				// 不支持断点续传：重置已下载大小
				if(!this.range) {
					downloadSize = 0L;
				}
				while(this.downloadable()) {
					length = this.input.read(buffer);
					if(length >= 0) {
						buffer.flip();
						this.output.write(buffer);
						buffer.clear();
						downloadSize += length;
						this.hlsSession.download(length); // 设置下载速度
					}
					streamSession.heartbeat();
					if(StreamContext.checkFinish(length, downloadSize, this.size)) {
						this.completed = true;
						break;
					}
				}
			} catch (Exception e) {
				LOGGER.error("HLS下载异常：{}", this.link, e);
			} finally {
				streamSession.remove();
			}
		}
		this.release();
		if(this.completed) {
			LOGGER.debug("HLS文件下载完成：{}", this.link);
			this.hlsSession.remove(this);
			this.hlsSession.downloadSize(downloadSize); // 设置下载大小
			this.hlsSession.checkCompletedAndDone();
		} else {
			LOGGER.debug("HLS文件下载失败：{}", this.link);
			// 下载失败重新添加下载
			this.hlsSession.download(this);
		}
	}

	/**
	 * <p>判断是否可以下载</p>
	 * 
	 * @return 是否可以下载
	 */
	private boolean downloadable() {
		return !this.completed && this.hlsSession.downloadable();
	}
	
	/**
	 * <p>校验是否完成</p>
	 * <p>文件是否存在、大小是否正确</p>
	 * 
	 * @param downloadSize 已下载大小
	 * 
	 * @return 是否完成
	 */
	private boolean checkCompleted(final long downloadSize) {
		// 如果文件已经完成直接返回完成
		if(this.completed) {
			return this.completed;
		}
		final File file = new File(this.path);
		if(!file.exists()) {
			return false;
		}
		try {
			// 文件实际大小
			final var header = HTTPClient.newInstance(this.link).head();
			this.size = header.fileSize();
			return this.size == downloadSize;
		} catch (NetException e) {
			LOGGER.error("HLS文件校验异常：{}", this.link, e);
		}
		return false;
	}

	/**
	 * <p>创建{@linkplain #input 输入流}</p>
	 * 
	 * @param downloadSize 已下载大小
	 * 
	 * @throws NetException 网络异常
	 */
	private void buildInput(final long downloadSize) throws NetException {
		// HTTP客户端
		final var client = HTTPClient.newInstance(this.link, SystemConfig.CONNECT_TIMEOUT, SystemConfig.DOWNLOAD_TIMEOUT);
		// HTTP响应
		final HttpResponse<InputStream> response = client.range(downloadSize).get(BodyHandlers.ofInputStream());
		// 请求成功和部分请求成功
		if(HTTPClient.downloadable(response)) {
			final var headers = HttpHeaderWrapper.newInstance(response.headers());
			this.range = headers.range();
			this.input = Channels.newChannel(response.body());
			if(this.range) { // 支持断点续传
				headers.verifyBeginRange(downloadSize);
			}
		} else {
			throw new NetException("HLS客户端输入流创建失败");
		}
	}
	
	/**
	 * <p>创建{@linkplain #output 输出流}</p>
	 * 
	 * @throws NetException 网络异常
	 */
	private void buildOutput() throws NetException {
		try {
			// 如果TS文件小于缓存大小直接使用文件大小
			int bufferSize = DownloadConfig.getMemoryBufferByte();
			if(this.size > 0L) {
				bufferSize = (int) this.size;
			}
			BufferedOutputStream outputStream;
			if(this.range) {
				// 支持断点续传
				outputStream = new BufferedOutputStream(new FileOutputStream(this.path, true), bufferSize);
			} else {
				// 不支持断点续传
				outputStream = new BufferedOutputStream(new FileOutputStream(this.path), bufferSize);
			}
			this.output = Channels.newChannel(outputStream);
		} catch (FileNotFoundException e) {
			throw new NetException("HLS客户端输出流创建失败", e);
		}
	}

	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		LOGGER.debug("HLS客户端释放：{}", this.link);
		IoUtils.close(this.input);
		IoUtils.close(this.output);
	}
	
}
