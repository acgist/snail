package com.acgist.snail.downloader;

import com.acgist.snail.coder.DownloaderUrlDecoder;
import com.acgist.snail.downloader.http.HttpDownloader;
import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

/**
 * 下载器构建
 */
public class DownloaderBuilder {

	private String url;
	private TaskWrapper wrapper;
	private DownloaderUrlDecoder decoder;
	
	private DownloaderBuilder(String url) {
		this.url = url;
	}
	
	private DownloaderBuilder(TaskEntity entity) throws DownloadException {
		this.wrapper = new TaskWrapper(entity);
	}
	
	public static final DownloaderBuilder newBuilder(String url) {
		final DownloaderBuilder builder = new DownloaderBuilder(url);
		return builder;
	}
	
	public static final DownloaderBuilder newBuilder(TaskEntity entity) throws DownloadException {
		final DownloaderBuilder builder = new DownloaderBuilder(entity);
		return builder;
	}
	
	public void build() throws DownloadException {
		if(wrapper == null) {
			this.buildDecoder();
			this.buildWrapper();
		}
		this.submit();
	}
	
	/**
	 * 解码器
	 */
	private void buildDecoder() {
		this.decoder = DownloaderUrlDecoder.newDecoder(this.url);
	}
	
	/**
	 * 持久化到数据库
	 */
	private void buildWrapper() throws DownloadException {
		this.wrapper = decoder.buildTaskWrapper();
	}
	
	/**
	 * 执行下载
	 */
	private void submit() throws DownloadException {
		DownloaderManager.getInstance().submit(wrapper);
	}

	/**
	 * 新建下载
	 */
	public static final IDownloader build(TaskWrapper wrapper) throws DownloadException {
		var type = wrapper.entity().getType();
		switch (type) {
			case ftp:
				return null;
			case ed2k:
				return null;
			case http:
				return HttpDownloader.newInstance(wrapper);
			case torrent:
				return null;
		}
		throw new DownloadException("不支持的下载类型：" + type);
	}
	
}
