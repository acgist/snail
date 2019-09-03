package com.acgist.snail.downloader.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.Downloader;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>HTTP下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class HttpDownloader extends Downloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);
	
	private final byte[] bytes; // 速度byte
	private BufferedInputStream input; // 输入流
	private BufferedOutputStream output; // 输出流
	private HttpHeaderWrapper responseHeader; // 响应头
	
	private HttpDownloader(TaskSession taskSession) {
		super(taskSession);
		this.bytes = new byte[128 * 1024];
	}

	public static final HttpDownloader newInstance(TaskSession taskSession) {
		return new HttpDownloader(taskSession);
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
			length = this.input.read(bytes, 0, bytes.length); // TODO：阻塞线程，导致暂停不能正常结束。
			if(isComplete(length)) { // 是否完成
				this.complete = true;
				break;
			}
			this.output.write(this.bytes, 0, length);
			this.download(length);
		}
	}

	@Override
	public void release() {
		IoUtils.close(this.input);
		IoUtils.close(this.output);
	}
	
//	@Override
//	public void unlockDownload() {
//		try {
//			this.input.close();
//		} catch (Exception e) {
//			LOGGER.error("HTTP下载释放下载异常", e);
//		}
//	}
	
	/**
	 * 任务是否完成：长度-1或者下载数据等于任务长度。
	 */
	private boolean isComplete(int length) {
		final long size = this.taskSession.entity().getSize();
		final long downloadSize = this.taskSession.downloadSize();
		return length == -1 || size == downloadSize;
	}
	
	/**
	 * <p>创建下载流</p>
	 * <p>
	 * 端点续传设置（Range）：<br>
	 * Range：bytes=0-499：第 0-499 字节范围的内容<br>
	 * Range：bytes=500-999：第 500-999 字节范围的内容<br>
	 * Range：bytes=-500：最后 500 字节的内容<br>
	 * Range：bytes=500-：从第 500 字节开始到文件结束部分的内容<br>
	 * Range：bytes=0-0,-1：第一个和最后一个字节的内容<br>
	 * Range：bytes=500-600,601-999：同时指定几个范围的内容<br>
	 * </p>
	 */
	private void buildInput() {
		final var entity = this.taskSession.entity();
		final long size = FileUtils.fileSize(entity.getFile()); // 已下载大小
		final var client = HTTPClient.newInstance(entity.getUrl());
		HttpResponse<InputStream> response = null;
		try {
			response = client
				.header("Range", "bytes=" + size + "-") // 端点续传
				.get(BodyHandlers.ofInputStream());
		} catch (Exception e) {
			fail("HTTP请求失败");
			LOGGER.error("HTTP请求异常", e);
			return;
		}
		if(HTTPClient.ok(response) || HTTPClient.partialContent(response)) {
			this.responseHeader = HttpHeaderWrapper.newInstance(response.headers());
			this.input = new BufferedInputStream(response.body());
			if(this.responseHeader.range()) { // 支持断点续传
				final long begin = this.responseHeader.beginRange();
				if(size != begin) {
					LOGGER.warn("已下载大小和开始下载位置不相等，已下载大小：{}，开始下载位置：{}，响应头：{}", size, begin, this.responseHeader.allHeaders());
				}
				this.taskSession.downloadSize(size);
			} else {
				this.taskSession.downloadSize(0L);
			}
		} else if(HTTPClient.requestedRangeNotSatisfiable(response)) { // 无法满足的请求范围
			if(this.taskSession.downloadSize() == entity.getSize()) {
				this.complete = true;
			} else {
				fail("无法满足文件下载范围");
			}
		} else {
			fail("HTTP请求失败（" + response.statusCode() + "）");
		}
	}

	/**
	 * 创建输出流
	 */
	private void buildOutput() {
		final var entity = this.taskSession.entity();
		try {
			final long size = this.taskSession.downloadSize();
			if(size == 0L) {
				this.output = new BufferedOutputStream(new FileOutputStream(entity.getFile()), DownloadConfig.getMemoryBufferByte());
			} else { // 支持续传
				this.output = new BufferedOutputStream(new FileOutputStream(entity.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			fail("打开下载文件失败");
			LOGGER.error("打开下载文件流异常", e);
		}
	}
	
}
