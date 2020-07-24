package com.acgist.snail.pojo.session;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.net.hls.HlsClient;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;

/**
 * <p>HSL任务信息</p>
 * 
 * @author acgist
 * @version 1.4.1
 */
public final class HlsSession {

	/**
	 * <p>下载线程数量</p>
	 */
	private static final int POOL_SIZE = SystemConfig.getHlsThreadSize();
	
	/**
	 * <p>下载状态</p>
	 */
	private volatile boolean downloadable = false;
	/**
	 * <p>已下载大小</p>
	 */
	private final AtomicLong downloadSize;
	/**
	 * <p>任务信息</p>
	 */
	private final ITaskSession taskSession;
	/**
	 * <p>线程池</p>
	 */
	private final ExecutorService executor;
	/**
	 * <p>计数器</p>
	 */
	private CountDownLatch countDownLatch;
	
	private HlsSession(ITaskSession taskSession) {
		this.downloadSize = new AtomicLong();
		this.taskSession = taskSession;
		this.executor = SystemThreadContext.newExecutor(POOL_SIZE, POOL_SIZE, 1000, 60L, SystemThreadContext.SNAIL_THREAD_HLS);
	}
	
	/**
	 * <p>创建HLS任务信息</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return HLS任务信息
	 */
	public static final HlsSession newInstance(ITaskSession taskSession) {
		return new HlsSession(taskSession);
	}
	
	/**
	 * <p>开始下载</p>
	 */
	public void download() {
		final var links = this.taskSession.selectTorrentFiles();
		for (String link : links) {
			this.executor.submit(new HlsClient(link));
		}
	}
	
	/**
	 * <p>设置已下载大小</p>
	 * 
	 * @param size 已下载大小
	 */
	public void downloadSize(int size) {
		this.downloadSize.addAndGet(size);
		this.taskSession.setSize(this.downloadSize.get());
		this.taskSession.downloadSize(this.downloadSize.get());
	}
	
	/**
	 * <p>设置下载速度</p>
	 * 
	 * @param buffer 速度
	 */
	public void download(int buffer) {
		this.taskSession.statistics().download(buffer);
	}
	
	/**
	 * <p>判断是否可以下载</p>
	 * 
	 * @return 是否可以下载
	 */
	public boolean downloadable() {
		return this.downloadable;
	}
	
	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		SystemThreadContext.shutdown(this.executor);
		this.downloadable = false;
	}
	
}
