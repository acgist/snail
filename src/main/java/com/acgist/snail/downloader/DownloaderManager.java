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
 * <p>下载器管理器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DownloaderManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderManager.class);
	
	private static final DownloaderManager INSTANCE = new DownloaderManager();
	
	/**
	 * <p>协议管理器</p>
	 */
	private final ProtocolManager manager;
	/**
	 * <p>下载器线程池</p>
	 */
	private final ExecutorService executor;
	/**
	 * <p>下载器MAP</p>
	 * <p>key=任务ID，value=下载器。</p>
	 */
	private final Map<String, IDownloader> downloaderMap;
	
	private DownloaderManager() {
		this.manager = ProtocolManager.getInstance();
		this.executor = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_DOWNLOADER);
		this.downloaderMap = new ConcurrentHashMap<>(DownloadConfig.getSize());
	}
	
	public static final DownloaderManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>通过下载链接新建下载任务</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @throws DownloadException 下载异常
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
	 * <p>开始下载任务</p>
	 */
	public void start(TaskSession taskSession) throws DownloadException {
		final var downloader = this.submit(taskSession);
		if(downloader != null) {
			downloader.start();
		}
	}
	
	/**
	 * <p>添加下载任务</p>
	 * <p>不修改任务状态，只添加到下载器线程池。</p>
	 */
	public IDownloader submit(TaskSession taskSession) throws DownloadException {
		if(ProtocolManager.getInstance().available()) {
			synchronized (this) {
				if(taskSession == null) {
					return null;
				}
				var downloader = downloader(taskSession);
				if(downloader == null) {
					downloader = taskSession.buildDownloader();
				}
				if(downloader == null) {
					throw new DownloadException("下载器不存在");
				}
				this.downloaderMap.put(downloader.id(), downloader);
				return downloader;
			}
		} else {
			throw new DownloadException("下载协议未初始化");
		}
	}
	
	/**
	 * <p>暂停任务</p>
	 */
	public void pause(TaskSession taskSession) {
		downloader(taskSession).pause();
	}
	
	/**
	 * <p>刷新任务</p>
	 */
	public void refresh(TaskSession taskSession) {
		downloader(taskSession).refresh();
	}

	/**
	 * <p>删除任务</p>
	 * <p>界面上面立即删除，实际删除任务在后台进行。</p>
	 */
	public void delete(TaskSession taskSession) {
		final var entity = taskSession.entity();
		// 需要定义在线程外面，防止后面下载器从队列中移除后导致的空指针。
		final var downloader = downloader(taskSession);
		// 后台删除任务
		SystemThreadContext.submit(() -> downloader.delete());
		// 队列立即移除
		this.downloaderMap.remove(entity.getId());
		GuiHandler.getInstance().refreshTaskList();
	}
	
	/**
	 * <p>切换下载器</p>
	 * <p>不删除任务，移除任务的下载器，重新下载并创建下载器。</p>
	 */
	public void changeDownloaderRestart(TaskSession taskSession) throws DownloadException {
		final var entity = taskSession.entity();
		taskSession.removeDownloader(); // 删除旧下载器
		this.downloaderMap.remove(entity.getId());
		this.start(taskSession);
	}

	/**
	 * <p>获取下载器</p>
	 */
	private IDownloader downloader(TaskSession taskSession) {
		return this.downloaderMap.get(taskSession.entity().getId());
	}
	
	/**
	 * <p>获取下载任务列表</p>
	 */
	public List<TaskSession> tasks() {
		return this.downloaderMap.values().stream()
			.map(IDownloader::taskSession)
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>刷新下载</p>
	 * <p>如果没满下载任务数量，增加下载任务线程。</p>
	 * <p>如果超过下载任务数量，减小下载任务线程。</p>
	 * <p>下载完成，暂停等操作时刷新下载任务。</p>
	 */
	public void refresh() {
		synchronized (this) {
			final var downloaders = this.downloaderMap.values();
			// 当前运行的下载器数量
			final long count = downloaders.stream()
				.filter(IDownloader::running)
				.count();
			final int downloadSize = DownloadConfig.getSize();
			if(count == downloadSize) { // 等于时不操作
			} else if(count > downloadSize) { // 大于时暂停部分下载任务
				downloaders.stream()
					.filter(IDownloader::running)
					.skip(downloadSize)
					.forEach(IDownloader::pause);
			} else { // 小于时开始部分下载任务
				downloaders.stream()
					.filter(downloader -> downloader.taskSession().await())
					.limit(downloadSize - count)
					.forEach(downloader -> this.executor.submit(downloader));
			}
		}
	}

	/**
	 * <p>停止下载</p>
	 * <p>暂停所有任务，关闭下载线程池。</p>
	 */
	public void shutdown() {
		LOGGER.info("关闭下载器管理器");
		try {
			this.downloaderMap.values().stream()
				.filter(downloader -> downloader.taskSession().inThreadPool())
				.forEach(downloader -> downloader.pause());
		} catch (Exception e) {
			LOGGER.error("关闭下载器管理器异常", e);
		}
//		SystemThreadContext.shutdown(this.executor); // 不直接关闭，线程关闭需要时间。
	}

}
