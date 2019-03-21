package com.acgist.snail.downloader.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse.BodyHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.AbstractDownloader;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.utils.FileUtils;

/**
 * HTTP下载
 */
public class HttpDownloader extends AbstractDownloader {

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
		bytes = new byte[DownloadConfig.getBufferByte()];
		buildInput();
		buildOutput();
	}
	
	@Override
	public void download() throws IOException {
		int length = 0;
		long begin, end;
		while(ok()) {
			begin = System.currentTimeMillis();
			length = input.readNBytes(bytes, 0, bytes.length);
			if(isComplete(length)) { // 是否完成
				complete = true;
				break;
			}
			output.write(bytes, 0, length);
			statistics(length);
			end = System.currentTimeMillis();
			yield(end - begin);
		}
	}

	@Override
	public void release() {
		try {
			if(input != null) {
				input.close();
			}
		} catch (IOException e) {
			LOGGER.error("关闭输入流异常", e);
		}
		try {
			if(output != null) {
				output.flush(); // 刷新
				output.close();
			}
		} catch (IOException e) {
			LOGGER.error("关闭文件流失败", e);
		}
	}
	
	/**
	 * 任务是否完成：长度-1或者下载数据等于任务长度
	 */
	private boolean isComplete(int length) {
		long size = taskSession.entity().getSize();
		long downloadSize = taskSession.downloadSize();
		return length == -1 || size == downloadSize;
	}
	
	/**
	 * 端点续传：<br>
	 * Range: bytes=0-499 表示第 0-499 字节范围的内容<br>
	 * Range: bytes=500-999 表示第 500-999 字节范围的内容<br>
	 * Range: bytes=-500 表示最后 500 字节的内容<br>
	 * Range: bytes=500- 表示从第 500 字节开始到文件结束部分的内容<br>
	 * Range: bytes=0-0,-1 表示第一个和最后一个字节<br>
	 * Range: bytes=500-600,601-999 同时指定几个范围<br>
	 */
	private void buildInput() {
		var entity = taskSession.entity();
		long size = FileUtils.fileSize(entity.getFile()); // 已下载大小
		HttpClient client = HTTPClient.newClient();
		var request = HTTPClient.newRequest(entity.getUrl())
			.header("Range", "bytes=" + size + "-") // 端点续传
			.GET()
			.build();
		var response = HTTPClient.request(client, request, BodyHandlers.ofInputStream());
		this.responseHeader = HttpHeaderWrapper.newInstance(response.headers());
		if(HTTPClient.ok(response)) {
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
				complete = true;
			} else {
				fail("无法满足文件下载范围");
			}
		} else {
			fail("失败代码（" + response.statusCode() + "）");
		}
	}

	private void buildOutput() {
		var entity = taskSession.entity();
		try {
			long size = taskSession.downloadSize();
			if(size == 0L) {
				output = new BufferedOutputStream(new FileOutputStream(entity.getFile()), DownloadConfig.getMemoryBufferByte());
			} else { // 支持续传
				output = new BufferedOutputStream(new FileOutputStream(entity.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("打开下载文件流失败", e);
			fail("文件打开失败");
		}
	}
	
}
