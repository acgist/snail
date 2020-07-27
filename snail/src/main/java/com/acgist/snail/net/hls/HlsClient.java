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
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.HlsSession;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>HLS客户端</p>
 * 
 * @author acgist
 * @version 1.4.1
 */
public final class HlsClient implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(HlsClient.class);
	
	/**
	 * <p>下载字节缓存大小：{@value}</p>
	 */
	protected static final int EXCHANGE_BYTES_LENGTH = 16 * SystemConfig.ONE_KB;
	
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
	
	public HlsClient(String link, HlsSession hlsSession, ITaskSession taskSession) {
		this.link = link;
		final String fileName = FileUtils.fileNameFromUrl(link);
		this.path = Paths.get(taskSession.getFile(), fileName).toString();
		this.range = false;
		this.completed = false;
		this.hlsSession = hlsSession;
	}

	@Override
	public void run() {
		if(!this.hlsSession.downloadable()) {
			LOGGER.debug("任务已经不能下载：{}", this.link);
			return;
		}
		LOGGER.debug("下载文件：{}", this.link);
		long size = 0;
		if(this.checkCompleted()) {
			size = this.size;
			this.completed = true;
			LOGGER.debug("文件校验成功：{}-{}", this.link, this.path);
		} else {
			try {
				int length = 0;
				this.buildInput();
				this.buildOutput();
				final byte[] bytes = new byte[EXCHANGE_BYTES_LENGTH];
				while(this.hlsSession.downloadable()) {
					length = this.input.read(bytes, 0, bytes.length);
					if(this.isComplete(length, size)) {
						this.completed = true;
						break;
					}
					this.output.write(bytes, 0, length);
					this.hlsSession.download(length); // 设置下载速度
					size += length;
				}
			} catch (Exception e) {
				LOGGER.error("HLS下载异常：{}", this.link, e);
			}
		}
		IoUtils.close(this.input);
		IoUtils.close(this.output);
		this.hlsSession.downloadSize(size); // 设置下载大小
		if(this.completed) {
			this.hlsSession.checkCompletedAndDone();
		} else {
			// 下载失败重新添加下载
			this.hlsSession.submit(this);
		}
	}
	
	/**
	 * <p>任务是否完成</p>
	 * 
	 * @return true-完成；false-没有完成；
	 */
	public boolean isCompleted() {
		return this.completed;
	}
	
	/**
	 * <p>校验文件</p>
	 * <p>文件是否存在、大小是否正确</p>
	 * 
	 * @return 校验是否成功
	 */
	private boolean checkCompleted() {
		if(this.completed) {
			return this.completed;
		}
		final File file = new File(this.path);
		if(!file.exists()) {
			return false;
		}
		// 已下载大小
		final long size = FileUtils.fileSize(this.path);
		try {
			// 文件实际大小
			final var header = HTTPClient.newInstance(this.link).head();
			this.size = header.fileSize();
			return this.size == size;
		} catch (NetException e) {
			LOGGER.error("HLS初始化失败：{}", this.link, e);
		}
		return false;
	}

	/**
	 * <p>验证是否下载完成</p>
	 * 
	 * @param length 当前下载大小
	 * @param size 累计下载大小
	 * 
	 * @return 是否完成
	 */
	private boolean isComplete(int length, long size) {
		return
			length <= -1 ||
			(this.size != 0 && size <= this.size);
	}
	
	/**
	 * <p>创建输入流</p>
	 * 
	 * @throws NetException 网络异常
	 */
	private void buildInput() throws NetException {
		// 已下载大小
		final long size = FileUtils.fileSize(this.path);
		// HTTP客户端
		final var client = HTTPClient.newInstance(this.link, SystemConfig.CONNECT_TIMEOUT, SystemConfig.DOWNLOAD_TIMEOUT);
		// HTTP响应
		final HttpResponse<InputStream> response = client
			.header(HttpHeaderWrapper.HEADER_RANGE, "bytes=" + size + "-")
			.get(BodyHandlers.ofInputStream());
		// 请求成功和部分请求成功
		if(
			HTTPClient.StatusCode.OK.verifyCode(response) ||
			HTTPClient.StatusCode.PARTIAL_CONTENT.verifyCode(response)
		) {
			final var headers = HttpHeaderWrapper.newInstance(response.headers());
			this.input = new BufferedInputStream(response.body());
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
			throw new NetException("创建输入流失败");
		}
	}
	
	/**
	 * <p>创建{@linkplain #output 输出流}</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void buildOutput() throws DownloadException {
		try {
			if(this.range) { // 支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(this.path, true), DownloadConfig.getMemoryBufferByte());
			} else { // 不支持断点续传
				this.output = new BufferedOutputStream(new FileOutputStream(this.path), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			throw new DownloadException("下载文件打开失败", e);
		}
	}

	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		
	}
	
}
