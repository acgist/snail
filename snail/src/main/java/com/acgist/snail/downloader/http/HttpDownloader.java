package com.acgist.snail.downloader.http;

import java.nio.channels.Channels;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.downloader.SingleFileDownloader;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>HTTP任务下载器</p>
 * 
 * @author acgist
 */
public final class HttpDownloader extends SingleFileDownloader {

	/**
	 * @param taskSession 任务信息
	 */
	private HttpDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * <p>新建HTTP任务下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link HttpDownloader}
	 */
	public static final HttpDownloader newInstance(ITaskSession taskSession) {
		return new HttpDownloader(taskSession);
	}
	
	@Override
	public void release() {
		IoUtils.close(this.input);
		IoUtils.close(this.output);
		super.release();
	}
	
	@Override
	protected void buildInput() throws NetException {
		final long downloadSize = FileUtils.fileSize(this.taskSession.getFile());
		final var client = HttpClient
			.newDownloader(this.taskSession.getUrl())
			.range(downloadSize)
			.get();
		if(client.downloadable()) {
			final var headers = client.responseHeader();
			this.input = Channels.newChannel(client.response());
			if(headers.range()) {
				this.taskSession.downloadSize(downloadSize);
			} else {
				this.taskSession.downloadSize(0L);
			}
		} else if(this.taskSession.downloadSize() == this.taskSession.getSize()) {
			// 优先验证下载文件大小
			// 416：超出请求范围
			this.completed = true;
		} else {
			this.fail("HTTP请求失败：" + client.code());
		}
	}

}
