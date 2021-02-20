package com.acgist.snail.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.IContext;
import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.exception.DownloadException;
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
	/**
	 * <p>下载协议上下文</p>
	 */
	private final ProtocolContext protocolContext;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private TaskContext() {
		this.tasks = new ArrayList<>(DownloadConfig.getSize());
		this.executor = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_DOWNLOADER);
		this.protocolContext = ProtocolContext.getInstance();
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
		final var session = this.protocolContext.buildTaskSession(url);
		session.start();
		return session;
	}
	
	/**
	 * <p>添加下载任务</p>
	 * <p>只添加下载任务，不修改任务状态。</p>
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
				// 任务添加必须创建下载器
				taskSession.buildDownloader();
				if(this.tasks.contains(taskSession)) {
					LOGGER.debug("任务已经存在：{}", taskSession.getName());
				} else {
					this.tasks.add(taskSession); // 添加任务
					GuiContext.getInstance().refreshTaskList(); // 刷新任务列表
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
			LOGGER.debug("删除下载任务：{}", taskSession.getName());
			this.tasks.remove(taskSession); // 删除任务
		}
		// 刷新任务列表
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
	 * <dl>
	 * 	<dt>刷新下载任务</dt>
	 * 	<dd>如果小于下载数量：增加下载任务线程</dd>
	 * 	<dd>如果大于下载数量：减小下载任务线程</dd>
	 * </dl>
	 */
	public void refresh() {
		synchronized (this.tasks) {
			// 当前任务正在下载数量
			final long downloadCount = this.tasks.stream()
				.filter(ITaskSession::statusDownload)
				.count();
			final int downloadSize = DownloadConfig.getSize();
			if(downloadCount == downloadSize) {
				// 等于：不操作
				LOGGER.debug("下载任务数量正常：{}-{}", downloadSize, downloadCount);
			} else if(downloadCount > downloadSize) {
				// 大于：暂停部分下载任务
				LOGGER.debug("暂停部分下载任务：{}-{}", downloadSize, downloadCount);
				this.tasks.stream()
					.filter(ITaskSession::statusDownload)
					.skip(downloadSize)
					.forEach(ITaskSession::await);
			} else {
				// 小于：开始部分下载任务
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
				} catch (Exception e) {
					LOGGER.error("添加下载任务异常：{}", entity.getName(), e);
					entityContext.delete(entity);
				}
			});
			// 刷新下载
			this.refresh();
		}
	}
	
	/**
	 * <p>关闭任务上下文</p>
	 * <p>暂停所有任务、关闭下载线程池</p>
	 */
	public void shutdown() {
		LOGGER.debug("关闭任务上下文");
		synchronized (this.tasks) {
			this.tasks.stream()
				.filter(ITaskSession::statusRunning)
				.forEach(ITaskSession::pause);
		}
		SystemThreadContext.shutdown(this.executor);
	}

}
