package com.acgist.snail.downloader.http;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.SingleFileDownloader;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>HTTP下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class HttpDownloader extends SingleFileDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);
	
	private HttpDownloader(TaskSession taskSession) {
		super(taskSession);
	}

	public static final HttpDownloader newInstance(TaskSession taskSession) {
		return new HttpDownloader(taskSession);
	}
	
	@Override
	public void release() {
		IoUtils.close(this.input);
		IoUtils.close(this.output);
		super.release();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>断点续传设置（Range）：</p>
	 * <pre>
	 * Range：bytes=0-499：第0-499字节范围的内容
	 * Range：bytes=500-999：第500-999字节范围的内容
	 * Range：bytes=-500：最后500字节的内容
	 * Range：bytes=500-：从第500字节开始到文件结束部分的内容
	 * Range：bytes=0-0,-1：第一个和最后一个字节的内容
	 * Range：bytes=500-600,601-999：同时指定多个范围的内容
	 * </pre>
	 */
	@Override
	protected void buildInput() {
		final var entity = this.taskSession.entity();
		// 获取已下载大小
		final long size = FileUtils.fileSize(entity.getFile());
		// 创建HTTP客户端
		final var client = HTTPClient.newInstance(entity.getUrl());
		HttpResponse<InputStream> response = null; // 响应
		try {
			response = client
				.header("Range", "bytes=" + size + "-")
				.get(BodyHandlers.ofInputStream());
		} catch (NetException e) {
			LOGGER.error("HTTP请求异常", e);
			fail("HTTP请求失败：" + e.getMessage());
			return;
		}
		if(HTTPClient.ok(response) || HTTPClient.partialContent(response)) {
			final var responseHeader = HttpHeaderWrapper.newInstance(response.headers());
			this.input = new BufferedInputStream(response.body());
			if(responseHeader.range()) { // 支持断点续传
				final long begin = responseHeader.beginRange();
				if(size != begin) {
					LOGGER.warn("已下载大小（{}）和开始下载位置（{}）不相等，HTTP响应头：{}", size, begin, responseHeader.allHeaders());
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

}
