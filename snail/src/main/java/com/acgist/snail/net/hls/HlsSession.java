package com.acgist.snail.net.hls;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.Cipher;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.IStatisticsSession;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.IMultifileCompletedChecker;
import com.acgist.snail.utils.BeanUtils;

/**
 * <p>HSL任务信息</p>
 * 
 * @author acgist
 */
public final class HlsSession implements IMultifileCompletedChecker {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HlsSession.class);

	/**
	 * <p>下载状态</p>
	 */
	private volatile boolean downloadable = false;
	/**
	 * <p>M3U8</p>
	 */
	private final M3u8 m3u8;
	/**
	 * <p>文件总数量</p>
	 */
	private final int fileSize;
	/**
	 * <p>线程池</p>
	 */
	private ExecutorService executor;
	/**
	 * <p>累计下载大小</p>
	 */
	private final AtomicLong downloadSize;
	/**
	 * <p>任务信息</p>
	 */
	private final ITaskSession taskSession;
	/**
	 * <p>HLS客户端</p>
	 * <p>HLS客户端下载成功移除列表，否者重新添加继续下载。</p>
	 */
	private final List<HlsClient> clients;
	/**
	 * <p>统计信息</p>
	 */
	private final IStatisticsSession statistics;
	/**
	 * 共享本地缓存
	 * 任务所有分片共享本地缓存，防止创建过多对象。
	 */
	private final ThreadLocal<ByteBuffer> threadLocal;
	
	/**
	 * @param m3u8 M3U8
	 * @param taskSession 任务信息
	 */
	private HlsSession(M3u8 m3u8, ITaskSession taskSession) {
		final var links = taskSession.multifileSelected();
		this.m3u8 = m3u8;
		this.fileSize = links.size();
		this.downloadSize = new AtomicLong();
		this.taskSession = taskSession;
		this.clients = new ArrayList<>(this.fileSize);
		this.statistics = taskSession.getStatistics();
		this.threadLocal = new ThreadLocal<>() {
			protected ByteBuffer initialValue() {
				return ByteBuffer.allocateDirect(SystemConfig.DEFAULT_EXCHANGE_LENGTH);
			};
		};
		links.stream()
		.map(link -> new HlsClient(link, taskSession, this, this.threadLocal))
		.forEach(this.clients::add);
	}
	
	/**
	 * <p>新建HLS任务信息</p>
	 * 
	 * @param m3u8 M3U8
	 * @param taskSession 任务信息
	 * 
	 * @return {@link HlsSession}
	 */
	public static final HlsSession newInstance(M3u8 m3u8, ITaskSession taskSession) {
		return new HlsSession(m3u8, taskSession);
	}
	
	/**
	 * <p>获取加密套件</p>
	 * 
	 * @return 加密套件
	 */
	public Cipher cipher() {
		if(this.m3u8 == null) {
			return null;
		}
		return this.m3u8.getCipher();
	}
	
	/**
	 * <p>开始下载</p>
	 * 
	 * @return 是否下载完成
	 */
	public boolean download() {
		if(this.downloadable) {
			LOGGER.debug("任务已经开始下载");
			return false;
		}
		// 修改开始下载：提交client需要判断
		this.downloadable = true;
		final int poolSize = SystemThreadContext.DEFAULT_THREAD_SIZE;
		this.executor = SystemThreadContext.newExecutor(poolSize, poolSize, 10000, 60L, SystemThreadContext.SNAIL_THREAD_HLS);
		synchronized (this.clients) {
			this.clients.forEach(this::download);
		}
		return this.checkCompleted();
	}
	
	/**
	 * <p>添加下载客户端</p>
	 * 
	 * @param client 客户端
	 */
	public void download(HlsClient client) {
		if(this.downloadable) {
			this.executor.submit(client);
		}
	}
	
	/**
	 * <p>移除下载客户端</p>
	 * 
	 * @param client 客户端
	 */
	public void remove(HlsClient client) {
		synchronized (this.clients) {
			this.clients.remove(client);
		}
	}
	
	/**
	 * <p>统计下载数据</p>
	 * 
	 * @param buffer 下载大小
	 */
	public void download(int buffer) {
		this.statistics.download(buffer);
		this.statistics.downloadLimit(buffer);
	}
	
	/**
	 * <p>设置已经下载大小</p>
	 * <p>注意：下载大小通过计算预计得出</p>
	 * 
	 * @param size 下载文件大小
	 */
	public void downloadSize(long size) {
		// 设置已经下载大小
		final long newDownloadSize = this.downloadSize.addAndGet(size);
		this.taskSession.setDownloadSize(newDownloadSize);
		// 已经下载文件数量
		int downloadFileSize;
		synchronized (this.clients) {
			downloadFileSize = this.fileSize - this.clients.size();
		}
		// 预测文件总大小：存在误差
		final long taskFileSize = newDownloadSize * this.fileSize / downloadFileSize;
		this.taskSession.setSize(taskFileSize);
	}
	
	/**
	 * <p>判断是否可以下载</p>
	 * 
	 * @return 是否可以下载
	 */
	public boolean downloadable() {
		return this.downloadable;
	}

	@Override
	public boolean checkCompleted() {
		synchronized (this.clients) {
			return this.clients.isEmpty();
		}
	}
	
	@Override
	public void checkCompletedAndUnlock() {
		if(this.checkCompleted()) {
			this.taskSession.unlockDownload();
		}
	}
	
	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		LOGGER.debug("HLS任务释放资源：{}", this.taskSession);
		this.downloadable = false;
		synchronized (this.clients) {
			this.clients.forEach(HlsClient::release);
		}
		SystemThreadContext.shutdownNow(this.executor);
	}

	/**
	 * <p>删除任务信息</p>
	 */
	public void delete() {
		HlsContext.getInstance().remove(this.taskSession);
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.taskSession.getName());
	}
	
}
