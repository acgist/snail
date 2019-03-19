package com.acgist.snail.downloader;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.DownloadException;

/**
 * 下载器管理
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
	 * 下载任务MAP
	 */
	private Map<String, IDownloader> TASK_MAP;
	
	static {
		INSTANCE.init();
	}
	
	private void init() {
		LOGGER.info("启动下载器管理");
		int downloadSize = DownloadConfig.getSize();
		TASK_MAP = new ConcurrentHashMap<>(downloadSize);
	}

	/**
	 * 修改同时下载任务数量：暂停所有任务-停止线程池-重新设置线程池大小-添加任务
	 */
	public void updateDownloadSize() {
		var list = TASK_MAP.entrySet()
		.stream()
		.map(Entry::getValue)
		.filter(downloader -> downloader.task().run())
		.collect(Collectors.toList());
		list.forEach(downloader -> {
			downloader.pause();
		});
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
		this.start(downloader.task());
	}
	
	/**
	 * 开始下载任务
	 */
	public void start(TaskSession session) throws DownloadException {
		this.submit(session).start();
	}
	
	/**
	 * 添加任务，不修改状态
	 */
	public void submit(IDownloader downloader) throws DownloadException {
		this.submit(downloader.task());
	}
	
	/**
	 * 添加任务，不修改状态
	 */
	public IDownloader submit(TaskSession session) throws DownloadException {
		synchronized (this) {
			if(session == null) {
				throw new DownloadException("下载任务不存在");
			}
			var entity = session.entity();
			IDownloader downloader = downloader(session);
			if(downloader == null) {
				downloader = session.downloader();
			}
			if(downloader == null) {
				throw new DownloadException("添加下载任务失败（下载任务为空）");
			}
			if(downloader.running()) {
				return downloader;
			}
			LOGGER.info("添加下载任务：{}", entity.getName());
			SystemThreadContext.submit(downloader);
			TASK_MAP.put(downloader.id(), downloader);
			return downloader;
		}
	}
	
	/**
	 * 暂停任务
	 */
	public void pause(TaskSession session) {
		downloader(session).pause();
	}
	
	/**
	 * 删除任务
	 */
	public void delete(TaskSession session) {
		var entity = session.entity();
		LOGGER.info("删除下载任务：{}", entity.getName());
		downloader(session).delete();
		TASK_MAP.remove(entity.getId());
	}

	/**
	 * 刷新任务
	 */
	public void refresh(TaskSession session) {
		downloader(session).refresh();
	}
	
	/**
	 * 获取下载任务
	 */
	private IDownloader downloader(TaskSession session) {
		return TASK_MAP.get(session.entity().getId());
	}
	
	/**
	 * 获取下载任务
	 */
	public List<TaskSession> tasks() {
		return DownloaderManager.getInstance().TASK_MAP.values()
			.stream()
			.map(IDownloader::task)
			.collect(Collectors.toList());
	}

	/**
	 * 停止下载：<br>
	 * 暂停任务<br>
	 * 关闭下载线程池
	 */
	public void shutdown() {
		LOGGER.info("关闭下载器管理");
		TASK_MAP.entrySet()
		.stream()
		.map(Entry::getValue)
		.filter(downloader -> downloader.task().run())
		.forEach(downloader -> downloader.pause());
	}
	
}
