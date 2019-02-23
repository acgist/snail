package com.acgist.snail.downloader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.config.DownloadConfig;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

/**
 * 下载器执行器
 */
public class DownloaderManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderManager.class);
	
	private static final DownloaderManager INSTANCE = new DownloaderManager();
	
	private DownloaderManager() {
	}
	
	public static final DownloaderManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 下载线程池
	 */
	private ExecutorService DOWNLOADER_EXECUTOR;
	/**
	 * 下载任务MAP
	 */
	private Map<String, IDownloader> DOWNLOADER_MAP;
	
	static {
		INSTANCE.init();
	}
	
	private void init() {
		LOGGER.info("初始化下载器管理");
		int downloadSize = DownloadConfig.getDownloadSize();
		LOGGER.info("初始化下载线程池，初始大小：{}", downloadSize);
		DOWNLOADER_EXECUTOR = Executors.newFixedThreadPool(downloadSize);
		DOWNLOADER_MAP = new ConcurrentHashMap<>(downloadSize);
	}
	
	/**
	 * 提交下载任务
	 */
	public void submit(IDownloader downloader) {
		LOGGER.info("开始任务：{}", downloader.name());
		DOWNLOADER_EXECUTOR.submit(downloader);
		DOWNLOADER_MAP.put(downloader.id(), downloader);
	}

	/**
	 * 获取下载任务
	 */
	public List<TaskWrapper> taskTable() {
		return DownloaderManager.getInstance().DOWNLOADER_MAP
			.values()
			.stream()
			.map(IDownloader::task)
			.collect(Collectors.toList());
	}

}
