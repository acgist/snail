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
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.utils.ThreadUtils;

/**
 * 下载器执行器
 * TODO：下载限速
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
	private Map<String, IDownloader> DOWNLOADER_TASK_MAP;
	
	static {
		INSTANCE.init();
	}
	
	private void init() {
		LOGGER.info("初始化下载器管理");
		int downloadSize = DownloadConfig.getDownloadSize();
		LOGGER.info("初始化下载线程池，初始大小：{}", downloadSize);
		DOWNLOADER_EXECUTOR = Executors.newFixedThreadPool(downloadSize, ThreadUtils.newThreadFactory("Downloader Thread"));
		DOWNLOADER_TASK_MAP = new ConcurrentHashMap<>(downloadSize);
	}
	
	/**
	 * 提交下载任务
	 */
	public void submit(IDownloader downloader) {
		TaskWrapper wrapper = downloader.taskWrapper();
		if(wrapper.getStatus() == Status.complete) {
			return;
		}
		LOGGER.info("开始任务：{}", downloader.name());
		DOWNLOADER_EXECUTOR.submit(downloader);
		DOWNLOADER_TASK_MAP.put(downloader.id(), downloader);
	}
	
	/**
	 * 开始任务
	 */
	public void start(TaskWrapper wrapper) {
		var downloader = downloader(wrapper);
		downloader.start();
		submit(downloader);
	}
	
	/**
	 * 暂停任务
	 */
	public void pause(TaskWrapper wrapper) {
		downloader(wrapper).pause();
	}
	
	/**
	 * 删除任务
	 */
	public void delete(TaskWrapper wrapper) {
		LOGGER.info("删除任务：{}", wrapper.getName());
		downloader(wrapper).delete();
		DOWNLOADER_TASK_MAP.remove(wrapper.getId());
	}

	/**
	 * 刷新任务
	 */
	public void refresh(TaskWrapper wrapper) {
		downloader(wrapper).refresh();
	}
	
	/**
	 * 获取下载任务
	 */
	private IDownloader downloader(TaskWrapper wrapper) {
		return DOWNLOADER_TASK_MAP.get(wrapper.getId());
	}
	
	/**
	 * 获取下载任务
	 */
	public List<TaskWrapper> taskTable() {
		return DownloaderManager.getInstance().DOWNLOADER_TASK_MAP.values()
			.stream()
			.map(IDownloader::taskWrapper)
			.collect(Collectors.toList());
	}

	/**
	 * 停止下载
	 */
	public void shutdown() {
		LOGGER.info("关闭下载器管理");
		DOWNLOADER_EXECUTOR.shutdown();
	}
	
}
