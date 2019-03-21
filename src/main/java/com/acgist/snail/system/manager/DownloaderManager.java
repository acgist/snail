package com.acgist.snail.system.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
		TASK_MAP = new ConcurrentHashMap<>(DownloadConfig.getSize());
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
		if(ProtocolManager.getInstance().available()) {
			synchronized (this) {
				if(session == null) {
					throw new DownloadException("下载任务不存在");
				}
				IDownloader downloader = downloader(session);
				if(downloader == null) {
					downloader = session.downloader();
				}
				if(downloader == null) {
					throw new DownloadException("添加下载任务失败（下载任务为空）");
				}
				TASK_MAP.put(downloader.id(), downloader);
				return downloader;
			}
		} else {
			throw new DownloadException("下载协议未初始化");
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
		return TASK_MAP.values().stream()
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
			var downloaders = TASK_MAP.values();
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
				.forEach(downloader -> SystemThreadContext.submit(downloader));
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
		TASK_MAP.values().stream()
		.filter(downloader -> downloader.task().coming())
		.forEach(downloader -> downloader.pause());
	}
	
}
