package com.acgist.snail.net.hls;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.StreamContext;
import com.acgist.snail.context.exception.DownloadException;
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
	 * <p>输入流</p>
	 */
	private InputStream input;
	/**
	 * <p>输出流</p>
	 */
	private OutputStream output;
	/**
	 * <p>HLS任务信息</p>
	 */
	private final HlsSession hlsSession;
	
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
		long size = FileUtils.fileSize(this.path);
		if(this.checkCompleted(size)) {
			this.completed = true;
			LOGGER.debug("HLS文件校验成功：{}", this.link);
		} else {
			int length = 0;
			final byte[] bytes = new byte[SystemConfig.DEFAULT_EXCHANGE_BYTES_LENGTH];
			final StreamSession streamSession = StreamContext.getInstance().newStreamSession(this.input);
			try {
				this.buildInput(size);
				this.buildOutput();
				// 不支持断点续传：重置已下载大小
				if(!this.range) {
					size = 0L;
				}
				while(this.hlsSession.downloadable()) {
					length = this.input.read(bytes, 0, bytes.length);
					if(this.checkCompleted(length, size)) {
						this.completed = true;
						break;
					}
					size += length;
					this.output.write(bytes, 0, length);
					streamSession.heartbeat();
					this.hlsSession.download(length); // 设置下载速度
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
			this.hlsSession.downloadSize(size); // 设置下载大小
			this.hlsSession.checkCompletedAndDone();
		} else {
			LOGGER.debug("HLS文件下载失败：{}", this.link);
			// 下载失败重新添加下载
			this.hlsSession.download(this);
		}
	}
	
	/**
	 * <p>校验文件</p>
	 * <p>文件是否存在、大小是否正确</p>
	 * 
	 * @param size 已下载大小
	 * 
	 * @return 校验是否成功
	 */
	private boolean checkCompleted(final long size) {
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
			return this.size == size;
		} catch (NetException e) {
			LOGGER.error("HLS客户端初始化异常：{}", this.link, e);
		}
		return false;
	}

	/**
	 * <p>判断是否下载完成</p>
	 * 
	 * @param length 下载数据大小
	 * @param size 已下载数据大小
	 * 
	 * @return 是否下载完成
	 */
	private boolean checkCompleted(int length, long size) {
		return
			// 没有更多数据
			length <= -1 ||
			// 已下载数据大小大于等于文件大小
			(this.size > 0L && this.size <= size);
	}
	
	/**
	 * <p>创建输入流</p>
	 * 
	 * @param size 已下载大小
	 * 
	 * @throws NetException 网络异常
	 */
	private void buildInput(final long size) throws NetException {
		// HTTP客户端
		final var client = HTTPClient.newInstance(this.link, SystemConfig.CONNECT_TIMEOUT, SystemConfig.DOWNLOAD_TIMEOUT);
		// HTTP响应
		final HttpResponse<InputStream> response = client.range(size).get(BodyHandlers.ofInputStream());
		// 请求成功和部分请求成功
		if(
			HTTPClient.StatusCode.OK.verifyCode(response) ||
			HTTPClient.StatusCode.PARTIAL_CONTENT.verifyCode(response)
		) {
			final var headers = HttpHeaderWrapper.newInstance(response.headers());
			this.input = new BufferedInputStream(response.body(), SystemConfig.DEFAULT_EXCHANGE_BYTES_LENGTH);
			if(headers.range()) { // 支持断点续传
				final long begin = headers.beginRange();
				if(size != begin) {
					// TODO：多行文本
					LOGGER.warn(
						"HTTP下载错误（已下载大小和开始下载位置不符），开始位置：{}，响应位置：{}，HTTP响应头部：{}",
						size, begin, headers.allHeaders()
					);
				}
				this.range = true;
			}
		} else {
			throw new NetException("HLS客户端输入流创建失败");
		}
	}
	
	/**
	 * <p>创建{@linkplain #output 输出流}</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void buildOutput() throws DownloadException {
		try {
			// 如果TS文件小于缓存大小直接使用文件大小
			int bufferSize = DownloadConfig.getMemoryBufferByte();
			if(this.size > 0L) {
				bufferSize = (int) this.size;
			}
			if(this.range) { // 支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(this.path, true), bufferSize);
			} else { // 不支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(this.path), bufferSize);
			}
		} catch (FileNotFoundException e) {
			throw new DownloadException("下载文件打开失败", e);
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
