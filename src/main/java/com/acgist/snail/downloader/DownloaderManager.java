package com.acgist.snail.downloader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.pojo.ITaskSession;
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
	 * <p>下载协议管理器</p>
	 */
	private final ProtocolManager manager;
	/**
	 * <p>下载器线程池</p>
	 */
	private final ExecutorService executor;
	/**
	 * <p>下载队列</p>
	 * <p>任务ID=下载器</p>
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
	 * <p>新建下载任务</p>
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
		} finally {
			GuiHandler.getInstance().refreshTaskList();
		}
	}
	
	/**
	 * <p>开始下载任务</p>
	 * 
	 * @param taskSession 下载任务
	 * 
	 * @throws DownloadException 下载异常
	 */
	public void start(ITaskSession taskSession) throws DownloadException {
		final var downloader = this.submit(taskSession);
		if(downloader != null) {
			downloader.start();
		}
	}
	
	/**
	 * <p>添加下载任务</p>
	 * <p>只将下载任务添加到下载队列，不修改任务状态。</p>
	 * 
	 * @param taskSession 下载任务
	 * 
	 * @return 下载器
	 * 
	 * @throws DownloadException 下载异常
	 */
	public IDownloader submit(ITaskSession taskSession) throws DownloadException {
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
					throw new DownloadException("创建下载器失败，下载协议：" + taskSession.getType());
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
	 * 
	 * @param taskSession 下载任务
	 */
	public void pause(ITaskSession taskSession) {
		downloader(taskSession).pause();
	}
	
	/**
	 * <p>刷新任务</p>
	 * 
	 * @param taskSession 下载任务
	 */
	public void refresh(ITaskSession taskSession) {
		downloader(taskSession).refresh();
	}

	/**
	 * <p>删除任务</p>
	 * <p>从{@linkplain #downloaderMap 下载队列}中立即删除，实际删除操作在后台进行。</p>
	 * 
	 * @param taskSession 下载任务
	 */
	public void delete(ITaskSession taskSession) {
		// 需要定义在后台删除任务外面：防止从下载队列中删除后导致空指针
		final var downloader = downloader(taskSession);
		// 后台删除任务
		SystemThreadContext.submit(() -> downloader.delete());
		// 下载队列删除
		this.downloaderMap.remove(taskSession.getId());
		// 刷新任务列表
		GuiHandler.getInstance().refreshTaskList();
	}
	
	/**
	 * <p>切换下载器</p>
	 * <p>先删除任务旧的下载器并从{@linkplain #downloaderMap 下载队列}中删除，然后重新下载。</p>
	 * 
	 * @param taskSession 下载任务
	 * 
	 * @throws DownloadException 下载异常
	 */
	public void changeDownloaderRestart(ITaskSession taskSession) throws DownloadException {
		taskSession.removeDownloader(); // 删除旧下载器
		this.downloaderMap.remove(taskSession.getId()); // 下载队列删除
		this.start(taskSession); // 重新下载
	}

	/**
	 * <p>获取下载任务的下载器</p>
	 * 
	 * @param taskSession 下载任务
	 * 
	 * @return 下载器
	 */
	private IDownloader downloader(ITaskSession taskSession) {
		return this.downloaderMap.get(taskSession.getId());
	}
	
	/**
	 * <p>获取所有下载任务列表</p>
	 * 
	 * @return 所有下载任务列表
	 */
	public List<ITaskSession> allTask() {
		return this.downloaderMap.values().stream()
			.map(IDownloader::taskSession)
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>获取任务信息</p>
	 * 
	 * @param id 任务ID
	 * 
	 * @return 任务信息
	 */
	public ITaskSession taskSession(String id) {
		final var downloader = this.downloaderMap.get(id);
		if(downloader == null) {
			return null;
		}
		return downloader.taskSession();
	}
	
	/**
	 * <dl>
	 * 	<dt>刷新下载任务</dt>
	 * 	<dd>如果小于下载任务数量：增加下载任务线程</dd>
	 * 	<dd>如果大于下载任务数量：减小下载任务线程</dd>
	 * </dl>
	 * <p>任务完成、暂停等操作时刷新下载任务</p>
	 */
	public void refresh() {
		synchronized (this) {
			final var downloaders = this.downloaderMap.values();
			// 当前任务下载数量
			final long count = downloaders.stream()
				.filter(IDownloader::downloading)
				.count();
			final int downloadSize = DownloadConfig.getSize();
			if(count == downloadSize) { // 等于：不操作
			} else if(count > downloadSize) { // 大于：暂停部分下载任务
				downloaders.stream()
					.filter(IDownloader::downloading)
					.skip(downloadSize)
					.forEach(IDownloader::pause);
			} else { // 小于：开始部分下载任务
				downloaders.stream()
					.filter(downloader -> downloader.taskSession().await())
					.limit(downloadSize - count)
					.forEach(downloader -> this.executor.submit(downloader));
			}
		}
	}

	/**
	 * <p>关闭下载器管理器</p>
	 * <p>暂停所有任务、关闭下载线程池</p>
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
//		SystemThreadContext.shutdown(this.executor); // 不直接关闭线程池：等待任务自动结束
	}

}
