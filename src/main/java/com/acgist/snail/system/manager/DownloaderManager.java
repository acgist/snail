package com.acgist.snail.system.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.IDownloader;
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
		executor = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_DOWNLOADER);
		tasks = new ConcurrentHashMap<>(DownloadConfig.getSize());
	}
	
	public static final DownloaderManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 任务线程池
	 */
	private final ExecutorService executor;
	/**
	 * 下载任务MAP
	 */
	private final Map<String, IDownloader> tasks;
	
	/**
	 * 开始下载任务
	 */
	public void start(IDownloader downloader) throws DownloadException {
		this.start(downloader.task());
	}
	
	/**
	 * 开始下载任务
	 */
	public void start(TaskSession taskSession) throws DownloadException {
		this.submit(taskSession).start();
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
	public IDownloader submit(TaskSession taskSession) throws DownloadException {
		if(ProtocolManager.getInstance().available()) {
			synchronized (this) {
				if(taskSession == null) {
					return null;
				}
				IDownloader downloader = downloader(taskSession);
				if(downloader == null) {
					downloader = taskSession.newDownloader();
				}
				if(downloader == null) {
					throw new DownloadException("添加下载任务失败（下载任务为空）");
				}
				tasks.put(downloader.id(), downloader);
				return downloader;
			}
		} else {
			throw new DownloadException("下载协议未初始化");
		}
	}
	
	/**
	 * 暂停任务
	 */
	public void pause(TaskSession taskSession) {
		downloader(taskSession).pause();
	}
	
	/**
	 * 删除任务
	 */
	public void delete(TaskSession taskSession) {
		var entity = taskSession.entity();
		downloader(taskSession).delete();
		tasks.remove(entity.getId());
	}

	/**
	 * 刷新任务
	 */
	public void refresh(TaskSession taskSession) {
		downloader(taskSession).refresh();
	}
	
	/**
	 * 获取下载任务
	 */
	private IDownloader downloader(TaskSession taskSession) {
		return tasks.get(taskSession.entity().getId());
	}
	
	/**
	 * 获取下载任务
	 */
	public List<TaskSession> tasks() {
		return tasks.values().stream()
			.map(IDownloader::task)
			.collect(Collectors.toList());
	}
	
	/**
	 * 刷新下载<br>
	 * 下载完成，暂停等操作时刷新下载任务
	 */
	public void refresh() {
		synchronized (this) {
			// 当前下载数量
			var downloaders = tasks.values();
			long count = downloaders.stream().filter(IDownloader::running).count();
			int downloadSize = DownloadConfig.getSize();
			if(count == downloadSize) { // 不操作
			} else if(count > downloadSize) { // 暂停部分操作
				downloaders.stream()
				.filter(IDownloader::running)
				.skip(downloadSize)
				.forEach(IDownloader::pause);
			} else { // 开始准备任务
				downloaders.stream()
				.filter(downloader -> downloader.task().await())
				.forEach(downloader -> executor.submit(downloader));
			}
		}
	}

	/**
	 * 新建下载任务<br>
	 * 通过下载链接生成下载任务
	 */
	public static final void submit(String url) throws DownloadException {
		ProtocolManager manager = ProtocolManager.getInstance();
		var session = manager.build(url);
		DownloaderManager.getInstance().submit(session);
		DownloaderManager.getInstance().refresh(); // 刷新下载
	}
	
	/**
	 * 停止下载：<br>
	 * 暂停任务<br>
	 * 关闭下载线程池
	 */
	public void shutdown() {
		LOGGER.info("关闭下载器管理");
		tasks.values().stream()
		.filter(downloader -> downloader.task().coming())
		.forEach(downloader -> downloader.pause());
	}
	
}
