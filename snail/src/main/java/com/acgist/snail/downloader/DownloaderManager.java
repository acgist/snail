package com.acgist.snail.downloader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.protocol.ProtocolManager;

/**
 * <p>下载器管理器</p>
 * 
 * @author acgist
 */
public final class DownloaderManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloaderManager.class);
	
	private static final DownloaderManager INSTANCE = new DownloaderManager();
	
	public static final DownloaderManager getInstance() {
		return INSTANCE;
	}
	
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
			GuiManager.getInstance().refreshTaskList();
		}
	}
	
	/**
	 * <p>开始下载任务</p>
	 * <p>添加下载任务并开始下载</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	public void start(ITaskSession taskSession) throws DownloadException {
		this.submit(taskSession).start();
	}
	
	/**
	 * <p>添加下载任务</p>
	 * <p>只添加下载任务，不修改任务状态。</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return 下载器
	 * 
	 * @throws DownloadException 下载异常
	 */
	public IDownloader submit(ITaskSession taskSession) throws DownloadException {
		if(ProtocolManager.getInstance().available()) {
			synchronized (this.downloaderMap) {
				if(taskSession == null) {
					throw new DownloadException("任务信息为空");
				}
				var downloader = this.downloader(taskSession);
				if(downloader == null) {
					downloader = taskSession.buildDownloader();
				}
				if(downloader == null) {
					throw new DownloadException("创建下载器失败（下载协议：" + taskSession.getType() + "）");
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
	 * @param taskSession 任务信息
	 */
	public void pause(ITaskSession taskSession) {
		this.downloader(taskSession).pause();
	}
	
	/**
	 * <p>刷新任务</p>
	 * 
	 * @param taskSession 任务信息
	 */
	public void refresh(ITaskSession taskSession) {
		this.downloader(taskSession).refresh();
	}

	/**
	 * <p>删除任务</p>
	 * <p>从{@linkplain #downloaderMap 下载队列}中立即删除，实际删除操作在后台进行。</p>
	 * 
	 * @param taskSession 任务信息
	 */
	public void delete(ITaskSession taskSession) {
		// 定义下载器：防止队列删除后后台删除空指针
		final var downloader = this.downloader(taskSession);
		// 后台删除任务
		SystemThreadContext.submit(() -> downloader.delete());
		// 下载队列删除
		this.downloaderMap.remove(taskSession.getId());
		// 刷新任务列表
		GuiManager.getInstance().refreshTaskList();
	}
	
	/**
	 * <p>切换下载器</p>
	 * <p>先删除任务旧下载器，然后从{@linkplain #downloaderMap 下载队列}中删除任务，最后重新下载。</p>
	 * 
	 * @param taskSession 任务信息
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
	 * @param taskSession 任务信息
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
	 * <dl>
	 * 	<dt>刷新下载任务</dt>
	 * 	<dd>如果小于下载任务数量：增加下载任务线程</dd>
	 * 	<dd>如果大于下载任务数量：减小下载任务线程</dd>
	 * </dl>
	 */
	public void refresh() {
		synchronized (this.downloaderMap) {
			final var downloaders = this.downloaderMap.values();
			// 当前任务正在下载数量
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
