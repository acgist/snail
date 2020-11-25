package com.acgist.snail.pojo.session;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>评分信息</p>
 * <p>评分不能初是为零，防止第一次直接被剔除。</p>
 * 
 * @author acgist
 */
public final class MarkSession {

	private static final Logger LOGGER = LoggerFactory.getLogger(MarkSession.class);
	
	/**
	 * <p>评分统计最短时间</p>
	 */
	private static final long MIN_MARK_INTERVAL = 60 * 1000L;
	
	/**
	 * <p>上传评分</p>
	 */
	private volatile long uploadMark = 8888;
	/**
	 * <p>下载评分</p>
	 */
	private volatile long downloadMark = 8888;
	/**
	 * <p>上次上传大小</p>
	 */
	private long lastUploadSize;
	/**
	 * <p>上次下载大小</p>
	 */
	private long lastDownloadSize;
	/**
	 * <p>累计上传大小</p>
	 */
	private final AtomicLong uploadSize = new AtomicLong(0);
	/**
	 * <p>累计下载大小</p>
	 */
	private final AtomicLong downloadSize = new AtomicLong(0);
	/**
	 * <p>最后一次刷新时间</p>
	 */
	private volatile long lastRefreshMarkTime = System.currentTimeMillis();
	
	public MarkSession() {
	}
	
	/**
	 * <p>刷新评分</p>
	 */
	private final void refreshMark() {
		final long nowTime = System.currentTimeMillis();
		final long interval = nowTime - this.lastRefreshMarkTime;
		if(interval > MIN_MARK_INTERVAL) {
			this.lastRefreshMarkTime = nowTime;
			final long uploadSize = this.uploadSize.get();
			this.uploadMark = uploadSize - this.lastUploadSize;
			this.lastUploadSize = uploadSize;
			final long downloadSize = this.downloadSize.get();
			this.downloadMark = downloadSize - this.lastDownloadSize;
			this.lastDownloadSize = downloadSize;
			LOGGER.debug("刷新评分：{}-{}", uploadSize, downloadSize);
		}
	}
	
	/**
	 * <p>上传计分</p>
	 * 
	 * @param buffer 上次大小
	 */
	public final void upload(int buffer) {
		this.uploadSize.addAndGet(buffer);
	}
	
	/**
	 * <p>获取上传评分</p>
	 * 
	 * @return 上传评分
	 */
	public final long uploadMark() {
		this.refreshMark();
		return this.uploadMark;
	}
	
	/**
	 * <p>下载计分</p>
	 * 
	 * @param buffer 下载大小
	 */
	public final void download(int buffer) {
		this.downloadSize.addAndGet(buffer);
	}
	
	/**
	 * <p>获取下载评分</p>
	 * 
	 * @return 下载评分
	 */
	public final long downloadMark() {
		this.refreshMark();
		return this.downloadMark;
	}
	
}
