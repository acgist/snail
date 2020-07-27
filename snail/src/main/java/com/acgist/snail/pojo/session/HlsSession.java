package com.acgist.snail.pojo.session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HlsSession.class);

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
	 * <p>HLS下载客户端</p>
	 */
	private final List<HlsClient> clients;
	
	private HlsSession(ITaskSession taskSession) {
		this.downloadable = true;
		this.downloadSize = new AtomicLong();
		this.taskSession = taskSession;
		this.executor = SystemThreadContext.newExecutor(POOL_SIZE, POOL_SIZE, 1000, 60L, SystemThreadContext.SNAIL_THREAD_HLS);
		this.clients = new ArrayList<>();
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
	 * <p>任务全部下载结束后，如果任务可以下载并且没有完成将继续下载。</p>
	 */
	public void download() {
		final var links = this.taskSession.multifileSelected();
		synchronized (this.clients) {
			for (String link : links) {
				final var client = new HlsClient(link, this, this.taskSession);
				this.clients.add(client);
				this.executor.submit(client);
			}
		}
	}
	
	/**
	 * <p>客户端下载失败重复添加</p>
	 * 
	 * @param hlsClient 客户端
	 */
	public void submit(HlsClient hlsClient) {
		this.executor.submit(hlsClient);
	}
	
	/**
	 * <p>设置已下载大小</p>
	 * 
	 * @param size 已下载大小
	 */
	public void downloadSize(long size) {
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
	 * <p>校验是否完成</p>
	 * 
	 * @return 是否完成
	 */
	public boolean checkCompleted() {
		return this.clients.stream().allMatch(HlsClient::isCompleted);
	}
	
	/**
	 * <p>校验是否完成</p>
	 * <p>如果完成解锁任务下载锁</p>
	 */
	public void checkCompletedAndDone() {
		final boolean completed = this.checkCompleted();
		if(completed) {
			final var downloader = this.taskSession.downloader();
			if(downloader != null) {
				downloader.unlockDownload(); // 解除下载锁
			}
		}
	}
	
	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		LOGGER.debug("释放任务：{}", this.taskSession.getName());
		synchronized (this.clients) {
			this.clients.forEach(client -> client.release());
		}
		SystemThreadContext.shutdown(this.executor);
		this.downloadable = false;
	}

}
