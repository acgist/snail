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
	private int size;
	/**
	 * <p>是否支持断点续传</p>
	 */
	private boolean range;
	/**
	 * <p>输入流</p>
	 */
	protected InputStream input;
	/**
	 * <p>输出流</p>
	 */
	protected OutputStream output;
	/**
	 * <p>HLS任务信息</p>
	 */
	private HlsSession hlsSession;
	
	public HlsClient(String link, ITaskSession taskSession) {
		this.link = link;
		this.range = false;
		final String fileName = FileUtils.fileNameFromUrl(link);
		this.path = Paths.get(taskSession.getFile(), fileName).toString();
	}

	@Override
	public void run() {
		LOGGER.debug("下载文件：{}", this.link);
		if(this.check()) {
			// 校验成功
		} else {
//			this.buildInput();
//			this.buildOutput();
//			final byte[] bytes = new byte[EXCHANGE_BYTES_LENGTH];
//			int length;
//			while(this.hlsSession.downloadable()) {
//				length = this.input.read(bytes, 0, bytes.length);
//				if(isComplete(length)) {
//					this.complete = true;
//					break;
//				}
//				this.output.write(bytes, 0, length);
//				this.download(length);
//				this.hlsSession.download(buffer); // 设置下载速度
//			}
		}
		this.hlsSession.downloadSize(size); // 设置下载大小
		IoUtils.close(this.input);
		IoUtils.close(this.output);
	}
	
	/**
	 * <p>校验文件</p>
	 * <p>文件是否存在、大小是否正确</p>
	 * 
	 * @return 校验是否成功
	 */
	private boolean check() {
		final File file = new File(this.path);
		if(!file.exists()) {
			return false;
		}
		// 已下载大小
		final long size = FileUtils.fileSize(this.path);
		// TODO：HTTP读取文件大小
		return false;
	}

	private boolean isComplete(int length) {
		return
			length <= -1;
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
		}
		throw new NetException("创建输入流失败");
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
	
}
