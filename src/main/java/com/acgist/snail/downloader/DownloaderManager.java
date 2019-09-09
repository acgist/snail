package com.acgist.snail.downloader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>下载器管理</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DownloaderManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderManager.class);
	
	private static final DownloaderManager INSTANCE = new DownloaderManager();
	
	private DownloaderManager() {
		this.manager = ProtocolManager.getInstance();
		this.executor = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_DOWNLOADER);
		this.downloaderMap = new ConcurrentHashMap<>(DownloadConfig.getSize());
	}
	
	public static final DownloaderManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 协议管理器
	 */
	private final ProtocolManager manager;
	/**
	 * 任务线程池
	 */
	private final ExecutorService executor;
	/**
	 * 下载任务MAP
	 */
	private final Map<String, IDownloader> downloaderMap;
	
	/**
	 * 新建下载任务
	 */
	public void newTask(String url) throws DownloadException {
		try {
			final var session = this.manager.buildTaskSession(url);
			if(session != null) {
				this.start(session);
			}
		} catch (DownloadException e) {
			throw e;
		} finally {
			GuiHandler.getInstance().refreshTaskList();
		}
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
	public IDownloader submit(TaskSession taskSession) throws DownloadException {
		if(ProtocolManager.getInstance().available()) {
			synchronized (this) {
				if(taskSession == null) {
					return null;
				}
				IDownloader downloader = downloader(taskSession);
				if(downloader == null) {
					downloader = taskSession.buildDownloader();
				}
				if(downloader == null) {
					throw new DownloadException("添加下载任务失败（下载任务为空）");
				}
				this.downloaderMap.put(downloader.id(), downloader);
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
	 * 刷新任务
	 */
	public void refresh(TaskSession taskSession) {
		downloader(taskSession).refresh();
	}

	/**
	 * <p>删除任务</p>
	 * <p>界面上面立即删除，实际删除任务在后台进行。</p>
	 */
	public void delete(TaskSession taskSession) {
		var entity = taskSession.entity();
		var downloader = downloader(taskSession); // 需要定义在线程外面，防止后面remove导致空指针。
		SystemThreadContext.submit(() -> { // 后台删除任务
			downloader.delete();
		});
		// 界面上立即移除
		this.downloaderMap.remove(entity.getId());
		GuiHandler.getInstance().refreshTaskList();
	}
	
	/**
	 * <p>切换下载器</p>
	 * <p>不删除任务，移除任务的下载器，下载时重新创建。</p>
	 */
	public void changeDownloaderRestart(TaskSession taskSession) throws DownloadException {
		var entity = taskSession.entity();
		taskSession.removeDownloader(); // 删除旧下载器
		this.downloaderMap.remove(entity.getId());
		this.start(taskSession);
	}

	/**
	 * 获取下载任务
	 */
	private IDownloader downloader(TaskSession taskSession) {
		return this.downloaderMap.get(taskSession.entity().getId());
	}
	
	/**
	 * 获取下载任务
	 */
	public List<TaskSession> tasks() {
		return this.downloaderMap.values().stream()
			.map(IDownloader::taskSession)
			.collect(Collectors.toList());
	}
	
	/**
	 * 刷新下载：如果没满下载任务数量，加入下载线程<br>
	 * 下载完成，暂停等操作时刷新下载任务
	 */
	public void refresh() {
		synchronized (this) {
			final var downloaders = this.downloaderMap.values();
			final long count = downloaders.stream()
				.filter(IDownloader::running)
				.count();
			final int downloadSize = DownloadConfig.getSize();
			if(count == downloadSize) { // 不操作
			} else if(count > downloadSize) { // 暂停部分操作
				downloaders.stream()
					.filter(IDownloader::running)
					.skip(downloadSize)
					.forEach(IDownloader::pause);
			} else { // 开始准备任务
				downloaders.stream()
					.filter(downloader -> downloader.taskSession().await())
					.limit(downloadSize - count)
					.forEach(downloader -> this.executor.submit(downloader));
			}
		}
	}

	/**
	 * <p>
	 * 停止下载：暂停所有任务、关闭下载线程池
	 * </p>
	 */
	public void shutdown() {
		LOGGER.info("关闭下载器管理");
		try {
			this.downloaderMap.values().stream()
				.filter(downloader -> downloader.taskSession().downloading())
				.forEach(downloader -> downloader.pause());
		} catch (Exception e) {
			LOGGER.error("关闭下载器管理异常", e);
		}
//		SystemThreadContext.shutdown(this.executor); // 不直接关闭，线程关闭需要时间
	}

}
