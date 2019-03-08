package com.acgist.snail.downloader;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.module.config.DownloadConfig;
import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

/**
 * 下载器管理器
 */
public final class DownloaderManager {

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
		int downloadSize = DownloadConfig.getSize();
		buildExecutor(downloadSize);
		DOWNLOADER_TASK_MAP = new ConcurrentHashMap<>(downloadSize);
	}

	/**
	 * 修改同时下载任务数量：暂停所有任务-停止线程池-重新设置线程池大小-添加任务
	 */
	public void updateDownloadSize() {
		int downloadSize = DownloadConfig.getSize();
		var list = DOWNLOADER_TASK_MAP.entrySet()
		.stream()
		.map(Entry::getValue)
		.filter(downloader -> downloader.wrapper().run())
		.collect(Collectors.toList());
		list.forEach(downloader -> {
			downloader.pause();
		});
		buildExecutor(downloadSize);
		list.forEach(downloader -> {
			try {
				start(downloader);
			} catch (DownloadException e) {
				LOGGER.error("添加下载任务异常", e);
			}
		});
	}
	
	/**
	 * 开始下载任务
	 */
	public void start(IDownloader downloader) throws DownloadException {
		this.start(downloader.wrapper());
	}
	
	/**
	 * 开始下载任务
	 */
	public void start(TaskWrapper wrapper) throws DownloadException {
		this.submit(wrapper).start();
	}
	
	/**
	 * 添加任务，不修改状态
	 */
	public void submit(IDownloader downloader) throws DownloadException {
		this.submit(downloader.wrapper());
	}
	
	/**
	 * 添加任务，不修改状态
	 */
	public IDownloader submit(TaskWrapper wrapper) throws DownloadException {
		if(wrapper == null) {
			return null;
		}
		var entity = wrapper.entity();
		IDownloader downloader = downloader(wrapper);
		if(downloader == null) {
			downloader = DownloaderBuilder.build(wrapper);
		}
		if(downloader == null) {
			throw new DownloadException("添加下载任务失败");
		}
		LOGGER.info("添加任务：{}", entity.getName());
		DOWNLOADER_EXECUTOR.submit(downloader);
		DOWNLOADER_TASK_MAP.put(downloader.id(), downloader);
		return downloader;
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
		var entity = wrapper.entity();
		LOGGER.info("删除任务：{}", entity.getName());
		downloader(wrapper).delete();
		DOWNLOADER_TASK_MAP.remove(entity.getId());
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
		return DOWNLOADER_TASK_MAP.get(wrapper.entity().getId());
	}
	
	/**
	 * 获取下载任务
	 */
	public List<TaskWrapper> taskTable() {
		return DownloaderManager.getInstance().DOWNLOADER_TASK_MAP.values()
			.stream()
			.map(IDownloader::wrapper)
			.collect(Collectors.toList());
	}

	/**
	 * 停止下载：<br>
	 * 暂停任务<br>
	 * 关闭下载线程池
	 */
	public void shutdown() {
		LOGGER.info("关闭下载器管理");
		DOWNLOADER_TASK_MAP.entrySet()
		.stream()
		.map(Entry::getValue)
		.filter(downloader -> downloader.wrapper().run())
		.forEach(downloader -> downloader.pause());
		DOWNLOADER_EXECUTOR.shutdown();
	}

	/**
	 * 创建下载线程池
	 */
	private void buildExecutor(int downloadSize) {
		if(DOWNLOADER_EXECUTOR != null) {
			DOWNLOADER_EXECUTOR.shutdown();
		}
		LOGGER.info("初始化下载线程池，初始大小：{}", downloadSize);
		DOWNLOADER_EXECUTOR = Executors.newFixedThreadPool(downloadSize, SystemThreadContext.newThreadFactory("Downloader Thread"));
	}
	
}
