package com.acgist.snail.downloader;

import com.acgist.snail.downloader.ftp.FtpDownloader;
import com.acgist.snail.downloader.http.HttpDownloader;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.protocol.DownloaderUrlDecoder;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.system.exception.DownloadException;

/**
 * 下载器构建
 */
public class DownloaderFactory {

	private String url;
	private TaskWrapper wrapper;
	private ProtocolManager manager = ProtocolManager.getInstance();
	
	private DownloaderFactory(String url) {
		this.url = url;
	}
	
	private DownloaderFactory(TaskEntity entity) throws DownloadException {
		this.wrapper = TaskWrapper.newInstance(entity);
	}
	
	/**
	 * 新建下载任务
	 */
	public static final DownloaderFactory newBuilder(String url) {
		final DownloaderFactory builder = new DownloaderFactory(url);
		return builder;
	}

	/**
	 * 数据库加载下载任务
	 */
	public static final DownloaderFactory newBuilder(TaskEntity entity) throws DownloadException {
		final DownloaderFactory builder = new DownloaderFactory(entity);
		builder.wrapper.loadDownloadSize(); // 加载已下载大小
		return builder;
	}
	
	/**
	 * 新建下载任务
	 */
	public void build() throws DownloadException {
		if(wrapper == null) {
			this.buildDecoder();
			this.buildWrapper();
		}
		this.submit();
	}
	
	/**
	 * 获取下载任务
	 */
	public TaskWrapper wrapper() {
		return this.wrapper;
	}
	
	/**
	 * 解码器
	 */
	private void buildDecoder() {
		manager.buildDownloader(url);
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
				return FtpDownloader.newInstance(wrapper);
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
