package com.acgist.snail.pojo.session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
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
	 * <p>文件总数量</p>
	 */
	private final int fileSize;
	/**
	 * <p>已下载文件总数量</p>
	 */
	private final AtomicInteger downloadFileSize;
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
	 * <p>下载链接列表</p>
	 */
	private final List<String> links;
	/**
	 * <p>HLS下载客户端</p>
	 */
	private final List<HlsClient> clients;
	
	private HlsSession(ITaskSession taskSession) {
		this.downloadable = true;
		this.downloadFileSize = new AtomicInteger();
		this.downloadSize = new AtomicLong();
		this.taskSession = taskSession;
		this.executor = SystemThreadContext.newExecutor(POOL_SIZE, POOL_SIZE, 1000, 60L, SystemThreadContext.SNAIL_THREAD_HLS);
		this.links = taskSession.multifileSelected();
		this.fileSize = this.links.size();
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
		synchronized (this.clients) {
			for (String link : this.links) {
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
	public void download(HlsClient hlsClient) {
		if(this.downloadable) {
			this.executor.submit(hlsClient);
		}
	}
	
	/**
	 * <p>下载完成移除客户端</p>
	 * 
	 * @param hlsClient 客户端
	 */
	public void remove(HlsClient hlsClient) {
		synchronized (this.clients) {
			this.clients.remove(hlsClient);
		}
	}
	
	/**
	 * <p>设置已下载大小</p>
	 * 
	 * @param size 已下载大小
	 */
	public void downloadSize(long size) {
		// 设置已下载大小
		final long downloadSize = this.downloadSize.addAndGet(size);
		this.taskSession.downloadSize(downloadSize);
		final int fileSize = this.downloadFileSize.incrementAndGet();
		// 预测文件总大小：存在误差
		final long taskFileSize = downloadSize * this.fileSize / fileSize;
		this.taskSession.setSize(taskFileSize);
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
		return this.downloadFileSize.get() >= this.fileSize;
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
		LOGGER.debug("HLS任务释放资源：{}", this.taskSession.getName());
		this.downloadable = false;
		synchronized (this.clients) {
			this.clients.forEach(client -> client.release());
		}
		this.clients.clear();
		SystemThreadContext.shutdown(this.executor);
	}

}
