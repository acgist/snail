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
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.utils.HttpUtils;

/**
 * 端点续传：https://www.cnblogs.com/findumars/p/5745345.html
 * 请求数据：
 * 		range: bytes=begin-end
 * 		content-range: bytes begin-end/total
 * 测试：http://my.163.com/zmb/
 * 
 * TODO：任务状态、断点续传
 */
public class HttpDownloader extends AbstractDownloader implements IDownloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloader.class);
	
	private byte[] bytes;
	private BufferedInputStream input;
	private BufferedOutputStream output;
	
	private HttpDownloader(TaskWrapper wrapper) {
		super(wrapper);
	}

	public static final HttpDownloader newInstance(TaskWrapper wrapper) {
		return new HttpDownloader(wrapper);
	}
	
	@Override
	public void open() {
		var entity = wrapper.entity();
		bytes = new byte[DownloadConfig.getDownloadBuffer() * 1024];
		try {
			output = new BufferedOutputStream(new FileOutputStream(entity.getFile()));
		} catch (FileNotFoundException e) {
			LOGGER.error("打开下载文件流失败", e);
			fail();
		}
		HttpClient client = HttpUtils.newClient();
		var request = HttpUtils.newRequest(entity.getUrl()).GET().build();
		var response = HttpUtils.request(client, request, BodyHandlers.ofInputStream());
		if(HttpUtils.ok(response)) {
			input = new BufferedInputStream(response.body());
		} else {
			fail();
		}
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
			if(length == -1) {
				complete = true;
				break;
			}
			output.write(bytes, 0, length);
			downloadSize(length);
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
			output.close();
		} catch (IOException e) {
			LOGGER.error("关闭文件流失败", e);
		}
	}
	
}
