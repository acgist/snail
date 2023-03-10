package com.acgist.snail.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.context.session.TaskSession;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.protocol.ProtocolContext;
import com.acgist.snail.utils.CollectionUtils;

/**
 * 任务上下文
 * 
 * @author acgist
 */
public final class TaskContext implements IContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskContext.class);
	
	private static final TaskContext INSTANCE = new TaskContext();
	
	public static final TaskContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 任务队列
	 */
	private final List<ITaskSession> tasks;
	/**
	 * 下载器线程池
	 */
	private final ExecutorService executor;
	
	private TaskContext() {
		this.tasks = new ArrayList<>(DownloadConfig.getSize());
		this.executor = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_DOWNLOADER);
	}
	
	/**
	 * 新建下载任务
	 * 添加下载任务同时开始下载
	 * 
	 * @param url 下载链接
	 * 
	 * @return 下载任务
	 * 
	 * @throws DownloadException 下载异常
	 */
	public ITaskSession download(String url) throws DownloadException {
		final ITaskSession session = ProtocolContext.getInstance().buildTaskSession(url);
		session.start();
		return session;
	}
	
	/**
	 * 添加下载任务
	 * 只添加下载任务不修改任务状态
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	public void submit(ITaskSession taskSession) throws DownloadException {
		if(ProtocolContext.getInstance().available()) {
			synchronized (this.tasks) {
				if(taskSession == null) {
					throw new DownloadException("任务信息为空");
				}
				// 任务添加必须新建下载器
				taskSession.buildDownloader();
				if(this.tasks.contains(taskSession)) {
					LOGGER.debug("任务已经存在：{}", taskSession);
				} else {
					this.tasks.add(taskSession);
					GuiContext.getInstance().refreshTaskList();
				}
			}
		} else {
			throw new DownloadException("下载协议未初始化");
		}
	}
	
	/**
	 * 删除下载任务
	 * 
	 * @param taskSession 下载任务
	 */
	public void remove(ITaskSession taskSession) {
		synchronized (this.tasks) {
			LOGGER.debug("删除下载任务：{}", taskSession);
			this.tasks.remove(taskSession);
		}
		GuiContext.getInstance().refreshTaskList();
	}
	
	/**
	 * @return 所有下载任务列表
	 */
	public List<ITaskSession> allTask() {
		synchronized (this.tasks) {
			return this.tasks.stream().collect(Collectors.toList());
		}
	}
	
	/**
	 * @return 是否含有下载任务
	 */
	public boolean running() {
		return this.allTask().stream()
			.anyMatch(ITaskSession::statusRunning);
	}
	
	/**
	 * 刷新下载任务
	 */
	public void refresh() {
		synchronized (this.tasks) {
			// 当前正在下载任务数量
			final long downloadCount = this.tasks.stream()
				.filter(ITaskSession::statusDownload)
				.count();
			final int downloadSize = DownloadConfig.getSize();
			if(downloadCount == downloadSize) {
				LOGGER.debug("下载任务数量正常：{}-{}", downloadSize, downloadCount);
			} else if(downloadCount > downloadSize) {
				LOGGER.debug("暂停部分下载任务：{}-{}", downloadSize, downloadCount);
				this.tasks.stream()
					.filter(ITaskSession::statusDownload)
					.skip(downloadSize)
					.forEach(ITaskSession::await);
			} else {
				LOGGER.debug("开始部分下载任务：{}-{}", downloadSize, downloadCount);
				this.tasks.stream()
					.filter(ITaskSession::statusAwait)
					.limit(downloadSize - downloadCount)
					.map(ITaskSession::downloader)
					.forEach(this.executor::submit);
			}
		}
	}

	/**
	 * 加载任务实体列表
	 */
	public void load() {
		final List<TaskEntity> list = EntityContext.getInstance().allTask();
		if(CollectionUtils.isNotEmpty(list)) {
			list.forEach(this::load);
			this.refresh();
		}
	}
	
	/**
	 * 加载任务实体
	 * 
	 * @param entity 任务实体
	 */
	private void load(TaskEntity entity) {
		try {
			final ITaskSession taskSession = TaskSession.newInstance(entity);
			// 重置状态
			taskSession.reset();
			this.submit(taskSession);
		} catch (Exception e) {
			LOGGER.error("添加下载任务异常：{}", entity, e);
			EntityContext.getInstance().delete(entity);
		}
	}
	
	/**
	 * 关闭任务上下文
	 */
	public void shutdown() {
		LOGGER.debug("关闭任务上下文");
		// 暂停所有任务
		synchronized (this.tasks) {
			this.tasks.stream()
				.filter(ITaskSession::statusRunning)
				.forEach(ITaskSession::pause);
		}
		SystemThreadContext.shutdown(this.executor);
	}

}
