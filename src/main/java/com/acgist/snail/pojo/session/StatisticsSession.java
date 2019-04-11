package com.acgist.snail.pojo.session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.utils.ThreadUtils;

/**
 * session - 统计<br>
 * 如果父类统计存在时更新同时更新父类统计
 */
public class StatisticsSession {

	private static final long ONE_SECOND = 1000L; // 一秒钟
	
	private final boolean limit; // 限速
	private final StatisticsSession parent; // 父类统计
	
	private AtomicLong uploadSize = new AtomicLong(0); // 已上传大小
	private AtomicLong downloadSize = new AtomicLong(0); // 已下载大小
	
	private long lastDownloadTime = System.currentTimeMillis(); // 最后一次统计时间
	private long downloadSecond = 0L; // 每秒下载速度
	private AtomicLong downloadBuffer = new AtomicLong(0); // 下载速度采样
	
	private long lastUploadTime = System.currentTimeMillis(); // 最后一次统计时间
	private long uploadSecond = 0L; // 每秒下载速度
	private AtomicLong uploadBuffer = new AtomicLong(0); // 下载速度采样
	
	public StatisticsSession() {
		this.limit = false;
		this.parent = null;
	}
	
	public StatisticsSession(StatisticsSession parent) {
		this.limit = false;
		this.parent = parent;
	}
	
	public StatisticsSession(boolean limit, StatisticsSession parent) {
		this.limit = limit;
		this.parent = parent;
	}

	/**
	 * 下载统计
	 */
	public void download(long buffer) {
		downloadSize.addAndGet(buffer);
		limitDownload(buffer);
		if(parent != null) {
			parent.download(buffer);
		}
	}
	
	/**
	 * 下载统计
	 */
	public void upload(long buffer) {
		uploadSize.addAndGet(buffer);
		limitUpload(buffer);
		if(parent != null) {
			parent.upload(buffer);
		}
	}
	
	/**
	 * 下载速度
	 */
	public long downloadSecond() {
		return downloadSecond;
	}
	
	/**
	 * 上传速度
	 */
	public long uploadSecond() {
		return uploadSecond;
	}
	
	/**
	 * 累计下载大小
	 */
	public long downloadSize() {
		return downloadSize.get();
	}
	
	/**
	 * 设置累计下载大小
	 */
	public void downloadSize(long size) {
		downloadSize.set(size);
	}
	
	/**
	 * 累计上传大小
	 */
	public long uploadSize() {
		return uploadSize.get();
	}

	/**
	 * 设置累计上传大小
	 */
	public void uploadSize(long size) {
		uploadSize.set(size);
	}
	
	/**
	 * 下载秒速统计限制
	 */
	private void limitDownload(long buffer) {
		final int limitSize = DownloadConfig.getBufferByte();
		final long oldDownloadBuffer = downloadBuffer.addAndGet(buffer);
		if(oldDownloadBuffer > limitSize) { // 超过限速
			synchronized (this) {
				if(oldDownloadBuffer == downloadBuffer.get()) {
					long interval = System.currentTimeMillis() - lastDownloadTime;
					if(limit) {
						final long expectTime = BigDecimal.valueOf(oldDownloadBuffer)
							.divide(BigDecimal.valueOf(limitSize), RoundingMode.HALF_UP)
							.multiply(BigDecimal.valueOf(ONE_SECOND))
							.longValue();
						if(interval < expectTime) { // 限速
							ThreadUtils.sleep(expectTime - interval);
							interval = expectTime;
						}
					}
					downloadSecond = oldDownloadBuffer * 1000 / interval;
					lastDownloadTime = System.currentTimeMillis();
					downloadBuffer.set(0); // 清空
				}
			}
		}
	}
	
	/**
	 * 上传秒速统计限制：上传限速=下载限速
	 */
	private void limitUpload(long buffer) {
		final int limitSize = DownloadConfig.getBufferByte();
		final long oldUploadBuffer = uploadBuffer.addAndGet(buffer);
		if(oldUploadBuffer > limitSize) { // 超过限速
			synchronized (this) {
				if(oldUploadBuffer == uploadBuffer.get()) {
					long interval = System.currentTimeMillis() - lastUploadTime;
					if(limit) {
						final long expectTime = BigDecimal.valueOf(oldUploadBuffer)
							.divide(BigDecimal.valueOf(limitSize), RoundingMode.HALF_UP)
							.multiply(BigDecimal.valueOf(ONE_SECOND))
							.longValue();
						if(interval < expectTime) { // 限速
							ThreadUtils.sleep(expectTime - interval);
							interval = expectTime;
						}
					}
					uploadSecond = oldUploadBuffer * 1000 / interval;
					lastUploadTime = System.currentTimeMillis();
					uploadBuffer.set(0); // 清空
				}
			}
		}
	}
	
}
