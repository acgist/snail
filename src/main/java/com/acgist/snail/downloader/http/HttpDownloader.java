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
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>HTTP下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class HttpDownloader extends Downloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);
	
	private byte[] bytes; // 速度byte
	private BufferedInputStream input; // 输入流
	private BufferedOutputStream output; // 输出流
	private HttpHeaderWrapper responseHeader; // 响应头
	
	private HttpDownloader(TaskSession taskSession) {
		super(taskSession);
	}

	public static final HttpDownloader newInstance(TaskSession taskSession) {
		return new HttpDownloader(taskSession);
	}
	
	@Override
	public void open() {
		bytes = new byte[1024 * 1024];
		buildInput();
		buildOutput();
	}
	
	@Override
	public void download() throws IOException {
		LOGGER.debug("HTTP任务开始下载");
		int length = 0;
		while(ok()) {
			length = input.readNBytes(bytes, 0, bytes.length);
			if(isComplete(length)) { // 是否完成
				this.complete = true;
				break;
			}
			output.write(bytes, 0, length);
			statistics(length);
		}
	}

	@Override
	public void release() {
		try {
			if(input != null) {
				input.close();
			}
		} catch (IOException e) {
			LOGGER.error("关闭HTTP输入流异常", e);
		}
		try {
			if(output != null) {
				output.flush(); // 刷新
				output.close();
			}
		} catch (IOException e) {
			LOGGER.error("关闭HTTP输出流异常", e);
		}
	}
	
	/**
	 * 任务是否完成：长度-1或者下载数据等于任务长度
	 */
	private boolean isComplete(int length) {
		final long size = taskSession.entity().getSize();
		final long downloadSize = taskSession.downloadSize();
		return length == -1 || size == downloadSize;
	}
	
	/**
	 * <p>端点续传：</p>
	 * <p>
	 * Range：bytes=0-499：第 0-499 字节范围的内容<br>
	 * Range：bytes=500-999：第 500-999 字节范围的内容<br>
	 * Range：bytes=-500：最后 500 字节的内容<br>
	 * Range：bytes=500-：从第 500 字节开始到文件结束部分的内容<br>
	 * Range：bytes=0-0,-1：第一个和最后一个字节的内容<br>
	 * Range：bytes=500-600,601-999：同时指定几个范围的内容<br>
	 * </p>
	 */
	private void buildInput() {
		final var entity = taskSession.entity();
		final long size = FileUtils.fileSize(entity.getFile()); // 已下载大小
		final var client = HTTPClient.newClient();
		final var request = HTTPClient.newRequest(entity.getUrl())
			.header("Range", "bytes=" + size + "-") // 端点续传
			.GET()
			.build();
		HttpResponse<InputStream> response = null;
		try {
			response = HTTPClient.request(client, request, BodyHandlers.ofInputStream());
		} catch (NetException e) {
			fail("HTTP请求失败");
			LOGGER.error("HTTP请求异常", e);
			return;
		}
		if(HTTPClient.ok(response)) {
			this.responseHeader = HttpHeaderWrapper.newInstance(response.headers());
			input = new BufferedInputStream(response.body());
			if(responseHeader.range()) { // 支持断点续传
				long begin = responseHeader.beginRange();
				if(size != begin) {
					LOGGER.warn("已下载大小和开始下载位置不相等，已下载大小：{}，开始下载位置：{}，响应头：{}", size, begin, responseHeader.headers());
				}
				taskSession.downloadSize(size);
			} else {
				taskSession.downloadSize(0L);
			}
		} else if(HTTPClient.rangeNotSatisfiable(response)) { // 无法满足的请求范围
			if(taskSession.downloadSize() == entity.getSize()) {
				this.complete = true;
			} else {
				fail("无法满足文件下载范围");
			}
		} else {
			fail("HTTP请求失败（" + response.statusCode() + "）");
		}
	}

	private void buildOutput() {
		final var entity = taskSession.entity();
		try {
			final long size = taskSession.downloadSize();
			if(size == 0L) {
				output = new BufferedOutputStream(new FileOutputStream(entity.getFile()), DownloadConfig.getMemoryBufferByte());
			} else { // 支持续传
				output = new BufferedOutputStream(new FileOutputStream(entity.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			fail("打开下载文件失败");
			LOGGER.error("打开HTTP文件流异常", e);
		}
	}
	
}
