package com.acgist.snail.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.acgist.snail.IContext;
import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>任务上下文</p>
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
	 * <p>任务队列</p>
	 */
	private final List<ITaskSession> tasks;
	/**
	 * <p>下载器线程池</p>
	 */
	private final ExecutorService executor;
	
	private TaskContext() {
		this.tasks = new ArrayList<>(DownloadConfig.getSize());
		this.executor = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_DOWNLOADER);
	}
	
	/**
	 * <p>新建下载任务</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 下载任务
	 * 
	 * @throws DownloadException 下载异常
	 */
	public ITaskSession download(String url) throws DownloadException {
		final var session = ProtocolContext.getInstance().buildTaskSession(url);
		session.start();
		return session;
	}
	
	/**
	 * <p>添加下载任务</p>
	 * <p>只添加下载任务不修改任务状态</p>
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
	 * <p>删除下载任务</p>
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
	 * <p>获取所有下载任务列表</p>
	 * 
	 * @return 所有下载任务列表
	 */
	public List<ITaskSession> allTask() {
		synchronized (this.tasks) {
			return this.tasks.stream().collect(Collectors.toList());
		}
	}
	
	/**
	 * <p>判定是否还有任务下载</p>
	 * 
	 * @return 是否还有任务下载
	 */
	public boolean downloading() {
		return this.allTask().stream()
			.anyMatch(ITaskSession::statusRunning);
	}
	
	/**
	 * <p>刷新下载任务</p>
	 */
	public void refresh() {
		synchronized (this.tasks) {
			// 当前任务正在下载数量
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
	 * <p>加载实体任务</p>
	 */
	public void load() {
		final EntityContext entityContext = EntityContext.getInstance();
		final List<TaskEntity> list = entityContext.allTask();
		if(CollectionUtils.isNotEmpty(list)) {
			list.forEach(entity -> {
				try {
					final var taskSession = TaskSession.newInstance(entity);
					taskSession.reset();
					this.submit(taskSession);
				} catch (Exception e) {
					LOGGER.error("添加下载任务异常：{}", entity, e);
					entityContext.delete(entity);
				}
			});
			this.refresh();
		}
	}
	
	/**
	 * <p>关闭任务上下文</p>
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
