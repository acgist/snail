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
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.module.config.DownloadConfig;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.HttpUtils;

/**
 * HTTP下载
 */
public class HttpDownloader extends AbstractDownloader implements IDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);
	
	private byte[] bytes; // 速度byte
	private BufferedInputStream input; // 输入流
	private BufferedOutputStream output; // 输出流
	private HttpHeaderWrapper responseHeader; // 响应头
	
	private HttpDownloader(TaskWrapper wrapper) {
		super(wrapper);
	}

	public static final HttpDownloader newInstance(TaskWrapper wrapper) {
		return new HttpDownloader(wrapper);
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
		while(true) {
			if(!wrapper.download()) {
				break;
			}
			begin = System.currentTimeMillis();
			length = input.readNBytes(bytes, 0, bytes.length);
			if(isComplete(length)) {
				complete = true;
				break;
			}
			output.write(bytes, 0, length);
			statistical(length);
			end = System.currentTimeMillis();
			yield(end - begin);
		}
	}

	@Override
	public void release() {
		try {
			input.close();
		} catch (IOException e) {
			LOGGER.error("关闭输入流异常", e);
		}
		try {
			output.flush(); // 刷新
			output.close();
		} catch (IOException e) {
			LOGGER.error("关闭文件流失败", e);
		}
	}
	
	/**
	 * 任务是否完成：长度-1或者下载数据等于任务长度
	 */
	private boolean isComplete(int length) {
		long size = wrapper.entity().getSize();
		long downloadSize = wrapper.downloadSize();
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
		var entity = wrapper.entity();
		long size = FileUtils.fileSize(entity.getFile()); // 已下载大小
		HttpClient client = HttpUtils.newClient();
		var request = HttpUtils.newRequest(entity.getUrl())
			.header("Range", "bytes=" + size + "-") // 端点续传
			.GET()
			.build();
		var response = HttpUtils.request(client, request, BodyHandlers.ofInputStream());
		this.responseHeader = HttpHeaderWrapper.newInstance(response.headers());
		if(HttpUtils.ok(response)) {
			input = new BufferedInputStream(response.body());
			if(responseHeader.range()) { // 支持断点续传
				long begin = responseHeader.beginRange();
				if(size != begin) {
					LOGGER.warn("已下载大小和开始下载位置不相等，已下载大小：{}，开始下载位置：{}，响应头：{}", size, begin, responseHeader.map());
				}
				wrapper.downloadSize(size);
			} else {
				wrapper.downloadSize(0L);
			}
		} else {
			fail();
		}
	}

	private void buildOutput() {
		var entity = wrapper.entity();
		try {
			long size = wrapper.downloadSize();
			if(size == 0L) {
				output = new BufferedOutputStream(new FileOutputStream(entity.getFile()), DownloadConfig.getMemoryBufferByte());
			} else { // 支持续传
				output = new BufferedOutputStream(new FileOutputStream(entity.getFile(), true), DownloadConfig.getMemoryBufferByte());
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("打开下载文件流失败", e);
			fail();
		}
	}
	
}
