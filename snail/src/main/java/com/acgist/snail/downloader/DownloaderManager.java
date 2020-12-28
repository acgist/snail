package com.acgist.snail.downloader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.EntityContext;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.utils.CollectionUtils;

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
	 */
	private final List<IDownloader> downloaders;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private DownloaderManager() {
		this.manager = ProtocolManager.getInstance();
		this.executor = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_DOWNLOADER);
		this.downloaders = new ArrayList<>(DownloadConfig.getSize());
	}
	
	/**
	 * <p>新建下载任务</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 下载器
	 * 
	 * @throws DownloadException 下载异常
	 */
	public IDownloader newTask(String url) throws DownloadException {
		try {
			final var session = this.manager.buildTaskSession(url);
			return this.start(session);
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
	 * @return 下载器
	 * 
	 * @throws DownloadException 下载异常
	 */
	public IDownloader start(ITaskSession taskSession) throws DownloadException {
		final IDownloader downloader = this.submit(taskSession);
		downloader.start(); // 开始下载
		return downloader;
	}
	
	/**
	 * <p>重新添加下载</p>
	 * <p>先删除任务旧下载器，然后从{@linkplain #downloaders 下载队列}中删除任务，最后重新下载。</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return 下载器
	 * 
	 * @throws DownloadException 下载异常
	 */
	public IDownloader restart(ITaskSession taskSession) throws DownloadException {
		final IDownloader downloader = taskSession.removeDownloader(); // 删除旧下载器
		synchronized (this.downloaders) {
			this.downloaders.remove(downloader); // 下载队列删除
		}
		return this.start(taskSession); // 重新下载
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
			synchronized (this.downloaders) {
				if(taskSession == null) {
					throw new DownloadException("任务信息为空");
				}
				final var downloader = taskSession.buildDownloader();
				if(downloader == null) {
					throw new DownloadException("创建下载器失败" + taskSession);
				}
				if(this.downloaders.contains(downloader)) {
					LOGGER.debug("任务已经存在：{}", taskSession.getName());
				} else {
					this.downloaders.add(downloader);
				}
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
		taskSession.downloader().pause();
	}
	
	/**
	 * <p>刷新任务</p>
	 * 
	 * @param taskSession 任务信息
	 */
	public void refresh(ITaskSession taskSession) {
		taskSession.downloader().refresh();
	}

	/**
	 * <p>删除任务</p>
	 * <p>从{@linkplain #downloaders 下载队列}中立即删除，实际删除操作在后台进行。</p>
	 * 
	 * @param taskSession 任务信息
	 */
	public void delete(ITaskSession taskSession) {
		// 获取下载器：防止队列删除后后台删除空指针
		final var downloader = taskSession.downloader();
		// 后台删除任务
		SystemThreadContext.submit(downloader::delete);
		synchronized (this.downloaders) {
			// 下载队列删除
			this.downloaders.remove(downloader);
		}
		// 刷新任务列表
		GuiManager.getInstance().refreshTaskList();
	}
	
	/**
	 * <p>获取所有下载任务列表</p>
	 * 
	 * @return 所有下载任务列表
	 */
	public List<ITaskSession> allTask() {
		synchronized (this.downloaders) {
			return this.downloaders.stream()
				.map(IDownloader::taskSession)
				.collect(Collectors.toList());
		}
	}
		
	/**
	 * <dl>
	 * 	<dt>刷新下载任务</dt>
	 * 	<dd>如果小于下载数量：增加下载任务线程</dd>
	 * 	<dd>如果大于下载数量：减小下载任务线程</dd>
	 * </dl>
	 */
	public void refresh() {
		synchronized (this.downloaders) {
			// 当前任务正在下载数量
			final long downloadCount = this.downloaders.stream()
				.filter(downloader -> downloader.taskSession().download())
				.count();
			final int downloadSize = DownloadConfig.getSize();
			if(downloadCount == downloadSize) {
				// 等于：不操作
			} else if(downloadCount > downloadSize) {
				LOGGER.debug("暂停部分下载任务：{}-{}", downloadSize, downloadCount);
				// 大于：暂停部分下载任务
				this.downloaders.stream()
					.filter(downloader -> downloader.taskSession().download())
					.skip(downloadSize)
					.forEach(IDownloader::pause);
			} else {
				LOGGER.debug("开始部分下载任务：{}-{}", downloadSize, downloadCount);
				// 小于：开始部分下载任务
				this.downloaders.stream()
					.filter(downloader -> downloader.taskSession().await())
					.limit(downloadSize - downloadCount)
					.forEach(this.executor::submit);
			}
		}
	}

	/**
	 * <p>加载实体任务</p>
	 */
	public void loadTaskEntity() {
		final EntityContext entityContext = EntityContext.getInstance();
		// 加载异常删除重新创建数组
		final List<TaskEntity> list = new ArrayList<>(entityContext.allTask());
		if(CollectionUtils.isNotEmpty(list)) {
			list.forEach(entity -> {
				try {
					final var taskSession = TaskSession.newInstance(entity);
					taskSession.reset();
					this.submit(taskSession);
				} catch (DownloadException e) {
					LOGGER.error("添加下载任务异常：{}", entity.getName(), e);
					entityContext.delete(entity);
				}
			});
			// 刷新下载
			this.refresh();
			// 刷新状态
			GuiManager.getInstance().refreshTaskList();
		}
	}
	
	/**
	 * <p>关闭下载器管理器</p>
	 * <p>暂停所有任务、关闭下载线程池</p>
	 */
	public void shutdown() {
		LOGGER.info("关闭下载器管理器");
		try {
			synchronized (this.downloaders) {
				this.downloaders.stream()
					.filter(downloader -> downloader.taskSession().inThreadPool())
					.forEach(IDownloader::pause);
			}
		} catch (Exception e) {
			LOGGER.error("关闭下载器管理器异常", e);
		}
		SystemThreadContext.shutdown(this.executor);
	}

}
