package com.acgist.snail.downloader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.acgist.snail.service.ConfigService;

/**
 * 下载器执行器
 */
@Component
public class DownloaderManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderManager.class);
	
	@Autowired
	private ConfigService configService;
	
	/**
	 * 下载线程池
	 */
	private ExecutorService DOWNLOADER_EXECUTOR;
	/**
	 * 下载任务MAP
	 */
	private Map<String, IDownloader> DOWNLOADER_MAP;
	
	@PostConstruct
	private void init() {
		LOGGER.info("初始化下载器管理");
		int downloadSize = configService.getDownloadSize();
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

}
